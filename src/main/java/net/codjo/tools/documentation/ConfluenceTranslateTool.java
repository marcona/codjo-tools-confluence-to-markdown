package net.codjo.tools.documentation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.codjo.confluence.Attachment;
import net.codjo.confluence.ConfluenceException;
import net.codjo.confluence.Page;
import net.codjo.confluence.PageSummary;
import net.codjo.confluence.plugin.ConfluenceOperations;
import net.codjo.confluence.plugin.ConfluencePlugin;
import net.codjo.util.file.FileUtil;
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
        contentModifiers.add(new EndCodeTagModifier());
        contentModifiers.add(new BeginningCodeTagModifier());
        contentModifiers.add(new ConfluenceChangeLogModifier());
        contentModifiers.add(new LinksModifier());
        contentModifiers.add(new ConfluenceLibraryHeaderModifier());
        contentModifiers.add(new TagAsTableModifier("note", "warning.gif", "#FFFFCE"));
        contentModifiers.add(new TagAsTableModifier("warning", "forbidden.gif", "#FFCCCC"));
    }


    public static void main(String[] args) {
        ConfluencePlugin plugin = new ConfluencePlugin();
        try {
            configurePlugin(plugin, ConfluenceTranslateTool.class);

            plugin.start(null);

            final ConfluenceOperations operations = plugin.getOperations();
            final File targetDirectory = new File("C:\\dev\\projects\\codjo\\lib\\codjo-administration.wiki");

            ConfluenceTranslateTool translator = new ConfluenceTranslateTool(operations);
            final String confluenceLibraryPage = "agf-administration";

            translator.translate(operations, targetDirectory, confluenceLibraryPage);
        }
        catch (IOException e) {
            e.printStackTrace();
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


    void translate(ConfluenceOperations operations, File targetDirectory, final String confluenceLibraryPage)
          throws ConfluenceException, IOException {
        Page libraryMainPage = operations.getPage("framework", confluenceLibraryPage);
        convertToWiki(libraryMainPage, targetDirectory);
    }


    // Method doesn't work, we need a dedicated method in codjo-confluence
    void generateIssuesFromChangelog(final String spaceKey, final String label, final String libraryName)
          throws ConfluenceException {
/*        final List<BlogEntry> pagesByLabel = operations.getBlogEntriesByLabel(spaceKey, label);
        final Map<String, List<BlogEntry>> resultMap = new HashMap<String, List<BlogEntry>>();

        for (BlogEntry blogEntry : pagesByLabel) {
            if (blogEntry.getTitle().startsWith(libraryName)) {
                //Marche bien mais tres long TODO a décommenter
//            final BlogEntrySummary framework = operations.getBlogEntrySummary(spaceKey, blogEntry.getId());
//            System.out.println("blogEntry.getPublishDate() = " + framework.getPublishDate());

                final List<Label> labelsById = operations.getLabelsById(blogEntry.getId());
                String frameworkLabel = null;
                for (Label label1 : labelsById) {
                    if (label1.getName().startsWith("framework")) {
                        frameworkLabel = label1.getName();
                    }
                }
                //TODO Attention aux doublons : eg 1.93 et 1.112
                System.out.println("frameworkLabel = " + frameworkLabel);
                System.out.println("\tblogEntry.getTitle() = " + blogEntry.getTitle());
            }
        }*/
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


    private interface ContentModifier {
        String modifyContent(String initialContent);
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

                result.append("\r\n").append("- ![](wiki/attachments/lightbulb");
                if (confluenceMacroParameters.contains("useAspect=true")) {
                    result.append("_on.gif)");
                    result.append(" [[aspect|aspect in ").append(libraryName).append("]]");
                }
                else {
                    result.append(".gif)").append(" aspect");
                }

                result.append("\r\n").append("- ![](wiki/attachments/lightbulb");
                if (confluenceMacroParameters.contains("useSecurity=true")) {
                    result.append("_on.gif)");
                    result.append(" [[security|security in ").append(libraryName).append("]]");
                }
                else {
                    result.append(".gif)").append(" security");
                }

                result.append("\r\n").append("- ![](wiki/attachments/lightbulb");
                if (confluenceMacroParameters.contains("useWorkflow=true")) {
                    result.append("_on.gif)");
                    result.append(" [[workflow|workflow in ").append(libraryName).append("]]");
                }
                else {
                    result.append(".gif)").append(" workflow");
                }
                result.append(matcher.group(3));
                return result.toString();
            }
            return initialContent;
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
}
