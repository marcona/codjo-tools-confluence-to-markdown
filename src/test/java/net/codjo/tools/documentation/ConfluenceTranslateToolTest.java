package net.codjo.tools.documentation;
import java.io.File;
import java.io.IOException;
import net.codjo.confluence.Page;
import net.codjo.confluence.plugin.ConfluenceOperations;
import net.codjo.confluence.plugin.ConfluencePlugin;
import net.codjo.test.common.fixture.DirectoryFixture;
import net.codjo.util.file.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static net.codjo.test.common.matcher.JUnitMatchers.*;
/**
 *
 */
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
        ConfluenceTranslateTool migrator = new ConfluenceTranslateTool(operations);
        migrator.changeLog();
    }
}
