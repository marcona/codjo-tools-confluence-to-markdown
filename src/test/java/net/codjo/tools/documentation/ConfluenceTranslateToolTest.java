package net.codjo.tools.documentation;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import net.codjo.confluence.BlogEntry;
import net.codjo.confluence.Label;
import net.codjo.confluence.Page;
import net.codjo.confluence.plugin.ConfluenceOperations;
import net.codjo.confluence.plugin.ConfluencePlugin;
import net.codjo.test.common.LogString;
import net.codjo.test.common.fixture.DirectoryFixture;
import net.codjo.util.file.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static net.codjo.test.common.matcher.JUnitMatchers.*;

public class ConfluenceTranslateToolTest {
    private static ConfluenceOperations operations;
    DirectoryFixture fixture = DirectoryFixture.newTemporaryDirectoryFixture("markdown");


    @BeforeClass
    public static void initOperations() throws Exception {
        ConfluencePlugin plugin = new ConfluencePlugin();
        ConfluenceTranslateTool.configurePlugin(plugin, ConfluenceTranslateToolTest.class);
        plugin.start(null);

        operations = plugin.getOperations();
    }


    @Before
    public void setUp() throws Exception {
        fixture.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        fixture.doTearDown();
    }


    @Test
    public void test_translateCodjoAdministrationFromConfluenceProduction() throws Exception {
        ConfluenceTranslateTool migrator = new ConfluenceTranslateTool(operations);
        Page libraryMainPage = operations.getPage("framework", "agf-administration");
        migrator.convertToWiki(libraryMainPage, fixture.getCanonicalFile());

        assertFileContent("Home.md", getClass().getResource("markdown/Home.md").getPath(), fixture);
        assertFileContent("Guide Utilisateur IHM de codjo-administration.md",
                          getClass().getResource("markdown/Guide+Utilisateur+IHM+de+codjo-administration.md").getPath(),
                          fixture);
        assertFileContent("Mise en place de codjo-administration.md",
                          getClass().getResource("markdown/Mise+en+place+de+codjo-administration.md").getPath(),
                          fixture);
        assertFileContent("Utilisation de codjo-administration.md",
                          getClass().getResource("markdown/Utilisation+de+codjo-administration.md").getPath(),
                          fixture);

        assertFileExist(fixture, "/attachments", "apply.png");
        assertFileExist(fixture, "/attachments", "configuration.png");
        assertFileExist(fixture, "/attachments", "pause.png");
        assertFileExist(fixture, "/attachments", "play.png");
        assertFileExist(fixture, "/attachments", "reload.png");
        assertFileExist(fixture, "/attachments", "undo.gif");
        assertFileExist(fixture, "/attachments", "pilotage.png");
        assertFileExist(fixture, "/attachments", "pilotageDirectoryModified.png");

        assertFileExist(fixture, "/attachments", "forbidden.gif");
        assertFileExist(fixture, "/attachments", "warning.gif");
        assertFileExist(fixture, "/attachments", "lightbulb.gif");
        assertFileExist(fixture, "/attachments", "lightbulb_on.gif");
    }


    private void assertFileExist(DirectoryFixture fixture,
                                 final String directory,
                                 String fileName) {
        assertThat(new File(fixture.getPath() + directory, fileName).exists(), is(true));
    }


    private static void assertFileContent(String inputFilePath,
                                          String expectedFilePath,
                                          final DirectoryFixture fixture) throws IOException {
        assertThat(FileUtil.loadContent(new File(fixture.getCanonicalPath(), inputFilePath)),
                   is(FileUtil.loadContent(new File(expectedFilePath))));
    }


    @Test
    @Ignore
    public void test_getChangelog() throws Exception {
        final LogString logString = new LogString();

        ConfluenceTranslateTool migrator = buildMockConfluenceTranslateTool(logString);
        migrator.generateIssuesFromChangelog("framework", "agf-administration", "agf-administration", "myGithubAccount",
                                             "myGithubPassword");
        logString.assertContent(
              "postIssue(agf-administration - Internationalization of gui components, [framework-2-19, agf-administration]), "
              + "postIssue(agf-administration - Environment information, [framework-1-198, agf-administration, hot-topics]), "
              + "postIssue(agf-administration - Environment information, [framework-1-196, agf-administration]), "
              + "postIssue(agf-administration - Internationalization bootstrap, [framework-1-184, agf-administration]), "
              + "postIssue(agf-administration - Bug fix in administration panel, [agf-administration, framework-1-162]), "
              + "postIssue(agf-administration - Tri de la liste des fichiers de logs, [hot-topics, framework-1-116, agf-administration]), "
              + "postIssue(agf-administration - Modification dynamique du répertoire de log des audits, [framework-1-113, agf-administration]), "
              + "postIssue(agf-administration - Accès au guide utilisateur à partir de l'IHM, [framework-1-112, agf-administration]), "
              + "postIssue(agf-administration - Activation ou désactivation dynamique des audits (handlers et mémoire), [hot-topics, framework-1-112, agf-administration]), "
              + "postIssue(agf-administration - Configuration du répertoire de log, [framework-1-96, agf-administration]), postIssue(agf-administration - Consultation des logs du serveur, [framework-1-95, agf-administration, hot-topics]), "
              + "postIssue(agf-administration - Logs sur les handlers, [agf-administration, framework-1-95]), postIssue(agf-administration - Statistiques d'utilisation mémoire, [framework-1-93, agf-administration]), "
              + "postIssue(agf-administration - Activation des logs sur les handlers, [framework-1-93, agf-administration, hot-topics]), postIssue(agf-administration - Création de la librairie, [agf-administration, framework-1-92, hot-topics])");
    }


    @Test
    @Ignore
    public void test_getChangelogReleasetest() throws Exception {
        final LogString logString = new LogString();

        ConfluenceTranslateTool migrator = buildMockConfluenceTranslateTool(logString);
        migrator.generateIssuesFromChangelog("framework", "agf-release-test", "agf-release-test", "myGithubAccount",
                                             "myGithubPassword");
        logString.assertContent(
              "postIssue(agf-administration - Internationalization of gui components, [framework-2-19, agf-administration]), "
              + "postIssue(agf-administration - Environment information, [framework-1-198, agf-administration, hot-topics]), "
              + "postIssue(agf-administration - Environment information, [framework-1-196, agf-administration]), "
              + "postIssue(agf-administration - Internationalization bootstrap, [framework-1-184, agf-administration]), "
              + "postIssue(agf-administration - Bug fix in administration panel, [agf-administration, framework-1-162]), "
              + "postIssue(agf-administration - Tri de la liste des fichiers de logs, [hot-topics, framework-1-116, agf-administration]), "
              + "postIssue(agf-administration - Modification dynamique du répertoire de log des audits, [framework-1-113, agf-administration]), "
              + "postIssue(agf-administration - Accès au guide utilisateur à partir de l'IHM, [framework-1-112, agf-administration]), "
              + "postIssue(agf-administration - Activation ou désactivation dynamique des audits (handlers et mémoire), [hot-topics, framework-1-112, agf-administration]), "
              + "postIssue(agf-administration - Configuration du répertoire de log, [framework-1-96, agf-administration]), postIssue(agf-administration - Consultation des logs du serveur, [framework-1-95, agf-administration, hot-topics]), "
              + "postIssue(agf-administration - Logs sur les handlers, [agf-administration, framework-1-95]), postIssue(agf-administration - Statistiques d'utilisation mémoire, [framework-1-93, agf-administration]), "
              + "postIssue(agf-administration - Activation des logs sur les handlers, [framework-1-93, agf-administration, hot-topics]), postIssue(agf-administration - Création de la librairie, [agf-administration, framework-1-92, hot-topics])");
    }


    @Test
    public void test_confluenceLibraryHeader() throws Exception {
        ConfluenceTranslateTool migrator = new ConfluenceTranslateTool(operations);
        String modifiedContent = migrator.applyContentModifiers(
              "{library-idcard:agf-administration|family=Plugin AGF|useSecurity=true}\n"
              + "Cette librairie permet de gérer les serveurs applicatifs.\n"
              + "{library-idcard}");

        assertThat(modifiedContent, is("\r\n#### Fiche signalétique de codjo-administration\r\n"
                                       + "##### Description\n"
                                       + "Cette librairie permet de gérer les serveurs applicatifs.\n"
                                       + "##### Famille\r\n"
                                       + "Plugin AGF\r\n"
                                       + "##### Caratéristique\r\n"
                                       + "- ![](wiki/attachments/lightbulb.gif) aspect\r\n"
                                       + "- ![](wiki/attachments/lightbulb_on.gif) [[security|security in codjo-administration]]\r\n"
                                       + "- ![](wiki/attachments/lightbulb.gif) workflow"));
    }


    @Test
    public void test_inlineCodeTagModifier() throws Exception {
        ConfluenceTranslateTool migrator = new ConfluenceTranslateTool(operations);
        String modifiedContent = migrator.applyContentModifiers(
              "blablaabla\n"
              + "Cette {{librairie}} permet de gérer {{les}} serveurs applicatifs.\n"
              + "blablaabla");

        assertThat(modifiedContent, is("blablaabla\n"
                                       + "Cette ```librairie``` permet de gérer ```les``` serveurs applicatifs.\n"
                                       + "blablaabla"));
    }


    @Test
    public void test_boldAndUnderlineModifier() throws Exception {
        ConfluenceTranslateTool migrator = new ConfluenceTranslateTool(operations);
        String modifiedContent = migrator.applyContentModifiers(
              "blablaabla\n"
              + "Cette *librairie* permet de gérer les serveurs applicatifs.\n"
              + "Ceci est une +*liste*+\n"
              + "* point un\n"
              + "* point deux\n"
              + "blablaabla");

        assertThat(modifiedContent, is("blablaabla\n"
                                       + "Cette **librairie** permet de gérer les serveurs applicatifs.\n"
                                       + "Ceci est une <u>**liste**</u>\n"
                                       + "* point un\n"
                                       + "* point deux\n"
                                       + "blablaabla"
        ));
    }


    @Test
    public void test_wikiTableModifier() throws Exception {
        ConfluenceTranslateTool migrator = new ConfluenceTranslateTool(operations);
        String modifiedContent = migrator.applyContentModifiers("blablablaText before\n"
                                                                + "|| Titre || description || \n"
                                                                + "| [import|Utilisation de agf-release-test - Exemples#import] | Exemple typique de test release d'import. | \n"
                                                                + "| [broadcast|Utilisation de agf-release-test - Exemples#broadcast] | Exemple typique de test release d'export. | \n"
                                                                + "\n"
                                                                + "blablablaText after\n"
                                                                + "||Propriété || Description || Exemple||\n"
                                                                + "|*```broadcast.output.dir```*| Répertoire de sortie des exports|```D:/red/release-test/tmp``` |\n"
                                                                + "|*```broadcast.output.remote.dir```*| Répertoire de sortie des exports pour les tests en mode distant.|```D:/red/release-test/tmp```|"
                                                                + "\n"
                                                                + "blablablaText after\n");

        assertThat(modifiedContent, is("blablablaText before\n"
                                       + "<table>\n"
                                       + "<tr>\n"
                                       + "<th>Titre</th><th>description</th></tr>\n"
                                       + "<tr>\n"
                                       + "<td> [[import|Utilisation de codjo-release-test - Exemples#import]] </td>\n"
                                       + "<td> Exemple typique de test release d'import. </td>\n"
                                       + "</tr>\n"
                                       + "<tr>\n"
                                       + "<td> [[broadcast|Utilisation de codjo-release-test - Exemples#broadcast]] </td>\n"
                                       + "<td> Exemple typique de test release d'export. </td>\n"
                                       + "</tr>\n"
                                       + "</table> \n\n"
                                       + "blablablaText after\n"
                                       + "<table>\n"
                                       + "<tr>\n"
                                       + "<th>Propriété</th><th>Description</th><th>Exemple</th></tr>\n"
                                       + "<tr>\n"
                                       + "<td>**```broadcast.output.dir```**</td>\n"
                                       + "<td> Répertoire de sortie des exports</td>\n"
                                       + "<td>```D:/red/release-test/tmp``` </td>\n"
                                       + "</tr>\n"
                                       + "<tr>\n"
                                       + "<td>**```broadcast.output.remote.dir```**</td>\n"
                                       + "<td> Répertoire de sortie des exports pour les tests en mode distant.</td>\n"
                                       + "<td>```D:/red/release-test/tmp```</td>\n"
                                       + "</tr>\n"
                                       + "</table>\n"
                                       + "blablablaText after"));
    }


    private ConfluenceTranslateTool buildMockConfluenceTranslateTool(final LogString logString) {
        return new ConfluenceTranslateTool(operations) {
            @Override
            protected void postIssue(String libraryName,
                                     String githubAccount,
                                     String githubPassword,
                                     BlogEntry blogEntry,
                                     File blogEntryDirectory,
                                     List<Label> labelsById) throws IOException, URISyntaxException {
                assertThat(libraryName, is("agf-administration"));
                assertThat(githubAccount, is("myGithubAccount"));
                assertThat(githubPassword, is("myGithubPassword"));
                logString.call("postIssue", blogEntry.getTitle(), labelsById);
            }
        };
    }


    @Test
    public void test_linkInWikiTable() throws Exception {
        String input = "|UN|DEUX|[[TROIS|QUATRE]]| CINQ [[SIX|SEPT]]|";

        final String result = ConfluenceTranslateTool.transformTableLine(input);
        assertThat(result,
                   is("<td>UN</td>\n<td>DEUX</td>\n<td>[[TROIS|QUATRE]]</td>\n<td> CINQ [[SIX|SEPT]]</td>\n"));
    }
}
