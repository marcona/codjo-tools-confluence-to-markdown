package net.codjo.tools.documentation;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.codjo.confluence.Attachment;
import net.codjo.confluence.BlogEntry;
import net.codjo.confluence.ConfluenceException;
import net.codjo.confluence.Label;
import net.codjo.confluence.Page;
import net.codjo.confluence.PageSummary;
import net.codjo.confluence.plugin.ConfluenceOperations;
import net.codjo.confluence.plugin.ConfluencePlugin;
import net.codjo.util.file.FileUtil;
import net.codjo.util.system.WindowsExec;
/**
 *
 */
public class ConfluenceTranslateTool {
    private ConfluenceOperations operations;
    private List<ContentModifier> contentModifiers;


    public ConfluenceTranslateTool(ConfluenceOperations operations) {
        this.operations = operations;
        this.contentModifiers = new ArrayList<ContentModifier>();
        contentModifiers.add(new AttachmentsModifier());
        contentModifiers.add(new AgfToCodjoModifier());
        contentModifiers.add(new HeaderModifier());
        contentModifiers.add(new InlineCodeTagModifier());
        contentModifiers.add(new BoldCodeTagModifier());
        contentModifiers.add(new UnderLineCodeTagModifier());
        contentModifiers.add(new EndCodeTagModifier());
        contentModifiers.add(new BeginningCodeTagModifier());
        contentModifiers.add(new ConfluenceChangeLogModifier());
        contentModifiers.add(new LinksModifier());
        contentModifiers.add(new ConfluenceLibraryHeaderModifier());
        contentModifiers.add(new TagAsTableModifier("note", "warning.gif", "#FFFFCE"));
        contentModifiers.add(new TagAsTableModifier("warning", "forbidden.gif", "#FFCCCC"));
        contentModifiers.add(new WikiTableModifier());
    }


    public static void main(String[] args) {
        final String confluenceLibraryName;
        final File targetDirectory;
        if (args.length == 2) {
            confluenceLibraryName = args[0];
            String destinationDirectory = args[1];
            targetDirectory = new File(destinationDirectory);
        }
        else {
            throw new IllegalArgumentException("You should have two arguments.");
        }

        ConfluencePlugin plugin = new ConfluencePlugin();
        try {
            configurePlugin(plugin, ConfluenceTranslateTool.class);
            plugin.start(null);

            final ConfluenceOperations operations = plugin.getOperations();
            ConfluenceTranslateTool translator = new ConfluenceTranslateTool(operations);
            translator.translate(targetDirectory, confluenceLibraryName);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void configurePlugin(ConfluencePlugin plugin, Class clazz) throws IOException {
        Properties confluenceProperties = new Properties();
        confluenceProperties.load(clazz.getResourceAsStream("/confluence-server.properties"));
        plugin.getConfiguration().setServerUrl(confluenceProperties.getProperty("confluence.server.url"));
        plugin.getConfiguration().setUser(confluenceProperties.getProperty("confluence.user"));
        plugin.getConfiguration().setPassword(confluenceProperties.getProperty("confluence.password"));
    }


    void translate(File targetDirectory, final String confluenceLibraryPage)
          throws ConfluenceException, IOException {
        final String spaceKey = "framework";
        Page libraryMainPage = operations.getPage(spaceKey, confluenceLibraryPage);
        convertToWiki(libraryMainPage, targetDirectory);
        generateIssuesFromChangelog(spaceKey, confluenceLibraryPage, confluenceLibraryPage, "", "");
    }


    void generateIssuesFromChangelog(final String spaceKey,
                                     final String label,
                                     final String libraryName,
                                     String githubAccount, String githubPassword)
          throws ConfluenceException {
        final List<BlogEntry> pagesByLabel = operations.getBlogEntriesByLabel(spaceKey, label);

        final File blogEntryDirectory = new File("target/tempBlogContent");
        blogEntryDirectory.mkdir();

        for (BlogEntry blogEntry : pagesByLabel) {
            if (blogEntry.getTitle().startsWith(libraryName)) {
                try {

                    final List<Label> labels = operations.getLabelsById(blogEntry.getId());
                    postIssue(libraryName, githubAccount, githubPassword, blogEntry, blogEntryDirectory, labels);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    protected void postIssue(String libraryName,
                             String githubAccount,
                             String githubPassword,
                             BlogEntry blogEntry,
                             File blogEntryDirectory, List<Label> labelsById) throws IOException, URISyntaxException {

        final File blogEntryFile = new File(blogEntryDirectory, "tmp-blogContent.txt");
        if( blogEntryFile.exists()){
            blogEntryFile.delete();
        }                            
        FileUtil.saveContent(blogEntryFile, applyContentModifiers(blogEntry.getContent()));

        WindowsExec executor = new WindowsExec();
        StringBuilder cmd = new StringBuilder();
        cmd.append("cmd /c gh postIssue ");
        cmd.append(libraryName).append(" ");
        cmd.append(blogEntry.getTitle()).append(" ");
        cmd.append("closed").append(" ");
        cmd.append(blogEntryFile.getCanonicalPath());
        for (Label label : labelsById) {
            cmd.append(label.getName()).append(" ");
        }
        executor.exec(cmd.toString(), blogEntryDirectory);
    }


    public void convertToWiki(Page libraryMainPage, final File targetDirectory)
          throws IOException, ConfluenceException {
        String modifiedContent = libraryMainPage.getContent();
        FileUtil.saveContent(new File(targetDirectory.getCanonicalFile(), "Home.md"),
                             applyContentModifiers(modifiedContent), "UTF-8");

        AttachmentDownloader.makeAttachmentDirectoryIfNecessary(targetDirectory);

        final List<PageSummary> children = operations.getChildren(libraryMainPage.getId());
        for (PageSummary child : children) {
            final Page childPage = operations.getPage(child.getId());
            String childPageContent = childPage.getContent();
            String newChildPageTitle = (childPage.getTitle() + ".md").replaceAll("agf\\-", "codjo-");
            FileUtil.saveContent(new File(targetDirectory.getCanonicalPath(), newChildPageTitle),
                                 applyContentModifiers(childPageContent), "UTF-8");

            final List<Attachment> attachments = operations.getAttachments(child.getId());
            for (Attachment attachment : attachments) {
                AttachmentDownloader.downloadAttachment(attachment, targetDirectory);
            }
        }
    }


    String applyContentModifiers(String modifiedContent) {
        for (ContentModifier contentModifier : contentModifiers) {
            modifiedContent = contentModifier.modifyContent(modifiedContent);
        }
        return modifiedContent;
    }


    static List<TableBloc> extractTableBloc(String input) {
        List<TableBloc> tableBlocs = new ArrayList<TableBloc>();
        int i = 0;
        while (i >= 0) {
            i = input.indexOf("\n||", i) + 1;
            if (i == 0) {
                break;
            }
            final TableBloc tableBloc = new TableBloc(i);
            tableBloc.end = findNextEmptyLine(input, tableBloc.start, 0);
            tableBlocs.add(tableBloc);
        }
        return tableBlocs;
    }


    private static int findNextEmptyLine(String input, int startIndex, int endIndex) {
        final int fromIndex = startIndex > endIndex ? startIndex : endIndex;

        int indexOfNextNewligne = input.indexOf("\n", fromIndex);
        if (indexOfNextNewligne != -1) {
            final String substring = input.substring(startIndex, indexOfNextNewligne);

            if (substring.contains("|")) {
                return findNextEmptyLine(input, indexOfNextNewligne + 1, indexOfNextNewligne + 1);
            }
            else {
                return endIndex;
            }
        }
        else {
            return endIndex;
        }
    }


    static String transformTableLine(String input) {
        StringBuilder builder = new StringBuilder();
        final String[] strings = input.split("\\|");
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (!"".equals(string.trim())) {
                if (string.contains("[[")) {
                    i++;
                    builder.append("<td>").append(string).append("|").append(strings[i]).append("</td>\n");
                }
                else {
                    builder.append("<td>").append(string).append("</td>\n");
                }
            }
        }
        return builder.toString();
    }


    private String convertWikiTable(String initialContent) {
        String pattern = "\\|\\|(.*)\\|\\|(.*?)\\|(.*)\\|(.*?)";
        Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(initialContent);
        String columnHeader = "";
        String columnsContent = "";
        if (matcher.find()) {
            columnHeader = matcher.group(1);
            columnsContent = "|" + matcher.group(3) + "|";
        }
        final String[] columnHeaders = columnHeader.split("\\|\\|");
        final String[] columnContents = columnsContent.split("\n");

        StringBuilder builder = new StringBuilder("<table>\n");
        builder.append("<tr>\n");
        for (String header : columnHeaders) {
            builder.append("<th>").append(header.trim()).append("</th>");
        }
        builder.append("</tr>\n");

        for (String columnContent : columnContents) {
            final String line = transformTableLine(columnContent);
            builder.append("<tr>\n").append(line).append("</tr>\n");
        }
        builder.append("</table>");
        final String result;
        try {
            result = matcher.replaceAll(builder.toString());
        }
        catch (Exception e) {
            e.printStackTrace();

            return initialContent;
        }
        return result;
    }


    private interface ContentModifier {
        String modifyContent(String initialContent);
    }

    private class InlineCodeTagModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            String pattern = "\\{\\{(.*?)\\}\\}";
            Matcher matcher = Pattern.compile(pattern).matcher(initialContent);
            return matcher.replaceAll("```$1```");
        }
    }

    private class BoldCodeTagModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            String pattern = "\\*(.*?)\\*";
            Matcher matcher = Pattern.compile(pattern).matcher(initialContent);
            return matcher.replaceAll("\\*\\*$1\\*\\*");
        }
    }

    private class UnderLineCodeTagModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            String pattern = "\\+(.*?)\\+";
            Matcher matcher = Pattern.compile(pattern).matcher(initialContent);
            return matcher.replaceAll("<u>$1</u>");
        }
    }

    private class BeginningCodeTagModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            String pattern = "\\{code\\:(.*)\\}(.*)";
            Matcher matcher = Pattern.compile(pattern).matcher(initialContent);
            return matcher.replaceAll("```$1");
        }
    }

    private class ConfluenceChangeLogModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            String pattern = "\\{changelogs\\:(.*)\\}";
            Matcher matcher = Pattern.compile(pattern).matcher(initialContent);
            return matcher.replaceAll("");
        }
    }

    private class WikiTableModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            initialContent = initialContent.replaceAll("\\$", "CARACTEREDOLLARD");

            String result = initialContent;
            final List<TableBloc> tableBlocs = extractTableBloc(initialContent);
            if (!tableBlocs.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                int start = 0;
                for (TableBloc tableBloc : tableBlocs) {
                    String wikiTable = initialContent.substring(tableBloc.start, tableBloc.end);
                    String htmlTable = convertWikiTable(wikiTable);
                    builder.append(initialContent.substring(start, tableBloc.start));
                    builder.append(htmlTable);
                    start = tableBloc.end;
                }
                if (start < initialContent.length() - 1) {
                    builder.append(initialContent.substring(start, initialContent.length() - 1));
                }
                result = builder.toString();
            }
            return result.replaceAll("CARACTEREDOLLARD", "\\$");
        }
    }

    class ConfluenceLibraryHeaderModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            String pattern = "\\{library-idcard\\:(.*)\\}(.*)\\{library-idcard\\}(.*)";
            Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(initialContent);
            if (matcher.matches()) {
                StringBuilder result = new StringBuilder("\r\n#### Fiche signalétique de ");
                String confluenceMacroParameters = matcher.group(1);
                final String[] split = confluenceMacroParameters.split("\\|");
                String libraryName = split[0];
                result.append(libraryName).append("\r\n");
                String description = matcher.group(2);
                result.append("##### Description").append(description);
                String familyString = split[1];//family=Plugin AGF
                result.append("##### Famille\r\n").append(familyString.replace("family=", "")).append("\r\n");
                result.append("##### Caratéristique");

                addCaracteristicFor(result, confluenceMacroParameters, libraryName, "Aspect");
                addCaracteristicFor(result, confluenceMacroParameters, libraryName, "Security");
                addCaracteristicFor(result, confluenceMacroParameters, libraryName, "Workflow");

                result.append(matcher.group(3));
                return result.toString();
            }
            return initialContent;
        }


        private void addCaracteristicFor(StringBuilder result,
                                         String confluenceMacroParameters,
                                         String libraryName, final String caracteristicName) {
            result.append("\r\n").append("- ![](wiki/attachments/lightbulb");
            final String caracteristicInLowerCase = caracteristicName.toLowerCase();
            if (confluenceMacroParameters.contains("use" + caracteristicName + "=true")) {
                result.append("_on.gif)");
                result.append(" [[")
                      .append(caracteristicInLowerCase)
                      .append("|")
                      .append(caracteristicInLowerCase)
                      .append(" in ").append(libraryName).append("]]");
            }
            else {
                result.append(".gif)").append(" ").append(caracteristicInLowerCase);
            }
        }
    }

    private class EndCodeTagModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            return initialContent.replaceAll("\\{code\\}", "```");
        }
    }

    private class HeaderModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            return initialContent.replaceAll("h1\\.", "#")
                  .replaceAll("h2\\.", "##")
                  .replaceAll("h3\\.", "###")
                  .replaceAll("h4\\.", "####");
        }
    }

    private class AgfToCodjoModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            String pattern = "agf\\-(.*)";
            Matcher matcher = Pattern.compile(pattern, Pattern.MULTILINE).matcher(initialContent);
            return matcher.replaceAll("codjo-$1");
        }
    }

    private class AttachmentsModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            String pattern = "\\!(.*?)\\!";
            Matcher matcher = Pattern.compile(pattern).matcher(initialContent);
            return matcher.replaceAll("\\![Alt attribute text Here]\\(attachments/$1\\)");
        }
    }

    private class LinksModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            String pattern = "\\[(.*)\\|(.*)\\]";
            Matcher matcher = Pattern.compile(pattern, Pattern.MULTILINE).matcher(initialContent);
            return matcher.replaceAll("[[$1|$2]]");
        }
    }

    private class TagAsTableModifier implements ContentModifier {

        private String backgroundColor;
        private String icone;
        private String tagName;


        private TagAsTableModifier(String tagName, String icone, String backgroundColor) {
            this.backgroundColor = backgroundColor;
            this.icone = icone;
            this.tagName = tagName;
        }


        public String modifyContent(String initialContent) {
            String pattern = "\\{" + tagName + "\\}(.*?)\\{" + tagName + "\\}";
            Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(initialContent);
            final String htmlTableForNote = "\r\n<table style=\'background-color: " + backgroundColor + ";'>\r\n"
                                            + "       <colgroup><col width='24'><col></colgroup>\r\n"
                                            + "         <tr>\r\n"
                                            + "           <td valign='top'><img src='attachments/" + icone
                                            + "' width='16' height='16' align='absmiddle' alt='' border='0'></td>\r\n"
                                            + "           <td><p>$1</p></td>\r\n"
                                            + "          </tr>\r\n"
                                            + "</table>\r\n";
            return matcher.replaceAll(htmlTableForNote);
        }
    }

    private static class TableBloc {
        int start;
        Integer end;


        private TableBloc(int start) {
            this.start = start;
        }


        @Override
        public String toString() {
            return "start: " + start + " end: " + end;
        }
    }
}
