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
import net.codjo.confluence.DefaultSearchCriteria;
import net.codjo.confluence.Page;
import net.codjo.confluence.PageSummary;
import net.codjo.confluence.SearchCriteria.Match;
import net.codjo.confluence.SearchResult;
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
        contentModifiers.add(new ConfluenceLibraryHeaderModifier());
        contentModifiers.add(new HeaderModifier());
        contentModifiers.add(new EndCodeTagModifier());
        contentModifiers.add(new BeginningCodeTagModifier());
        contentModifiers.add(new ConfluenceChangeLogModifier());
        contentModifiers.add(new LinksModifier());
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
            translator.translate(operations, targetDirectory);
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


    void translate(ConfluenceOperations operations, File targetDirectory)
          throws ConfluenceException, IOException {
        Page libraryMainPage = operations.getPage("framework", "agf-administration");
        convertToWiki(libraryMainPage, targetDirectory);
    }


    void changeLog() throws ConfluenceException {
        final DefaultSearchCriteria searchCriteria = new DefaultSearchCriteria();
        searchCriteria
//              .withCriteria("type", "blogpost", Match.exact)
              .withCriteria("label", "framework", Match.exact);

        final List<SearchResult> pagesByLabel = operations.searchByCriteria("framework", searchCriteria, 100);
        for (SearchResult page : pagesByLabel) {
            System.out.println("page.getTitle() = " + page.getTitle());
        }
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


    private String applyContentModifiers(String modifiedContent) {
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

    private class ConfluenceLibraryHeaderModifier implements ContentModifier {
        public String modifyContent(String initialContent) {
            String pattern = "\\{library-idcard\\:(.*)\\}(.*)\\{library-idcard\\}";
            Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(initialContent);
            return matcher.replaceAll("Detail Panel: $1\r\n$2");
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
