Part of the Framework codjo.net
===================

This is a small tool to translate Confluence wiki content to Github wiki content.

To run the tool, you have to precise the following maven properties :
- confluencePrdUrl
- confluencePrdUser
- confluencePrdPassword

You'll also have to add the following repository OR activate the ```codjo``` profile :
* https://github.com/gonnot/codjo-install-workstation/blob/master/common/m2/settings.xml

NB : ```ConfluenceTranslateToolTest``` is in fact an integration test with our Confluence test server. I should mock the confluence server.

TODO :
 * manage accent in page title
 * manage anchors
 * manage underline and bold *X* becomes **X** and +X+ becomes <u>X</u> OK but doesn't work in table (inline html)
 * add unit tests foreach modifier
 * verify dead links management (unexisting wiki pages)
 * iterate on all codjo libraries, plugin and tools
 * enhance unit test by creating a mock confluence server ? see : https://developer.atlassian.com/display/CONFDEV/Writing+Unit+Tests+for+your+Plugin
 * insert confluence news history and pull request in changelog
    - to deal with old news :
      - in codjo-confluence
        Add search method for blogposts in ConfluenceOperations:
                  final List<SearchResult> searchResults = server.searchByLabelName("agf-administration");
        Add a RPC call :
                      public Page getBlogEntry(String pageId) throws ConfluenceException {
                          Map<String, String> page = (Map<String, String>)executeRemoteCall("confluence1.getBlogEntry", 0, token(),
                                                                                            pageId);
                          return new Page(page);
                      }
      - in confluence-to-markdown
          translate the page.getContent() result;
          See if we wat a new wikiPage for each news or a single page with all news or why not an issue ?
          See how to deal with framework versions (nb: github issues have "labels")
    - OR just make an html export from confluence ??


 * automatic upload of wiki on github
     - create wiki repository (only by the website cause github API can't deal with wiki site, should use release-test or Httpunit ?)
     - execute git clone git clone https://github.com/codjo/codjo-LIBRARY.wiki.git
        --> use command line
        --> or use jgit library see http://wiki.eclipse.org/JGit/User_Guide
     - launch TranslateTool
     - commit modifications in git
        --> use command line
        --> or use jgit library see http://wiki.eclipse.org/JGit/User_Guide
     - push new wiki
             --> use command line
             --> or use jgit library see http://wiki.eclipse.org/JGit/User_Guide


 * Reflexion on news process :
   - developper write an issue with label codjo-administration
   - try to understand link between issue and pull request --> during plateform meeting we close issues + pull request
   - when doing the stabilisation, create a new Label for the issue (cf github api and codjo plugin) with framework number

 
 DONE :
     - manage wiki table (see Utilisation-de-codjo-release-test)
     - deal with arguments : outputPath, main page name ...etc
     - {{pom.xml}} not traducted
     - Decide wether or not we translate the header menu "project reports | svn | recherche | mantis" --> not for the moment I don't want
        our sic to be publicly known ;-)
     - Detail panel to implement (with library name, security support and plugin
     - copy forbidden.gif and warning.gif to attachments
     - rename library to confluence-to-markdown
     - extract passwords from published code --> externalize in settings + plugin configuration file
     - rename agf-xxx by codjo-xxxx
     - pages should have .md extension
     - main page has to be "Home.md"

     - Remplace [ and ] by [[ and ]] for the link support
     - Replace hX. by Xtime '#'
     - Replace
          * {code:xml} by ```xml
          * {code} by ```
     - deal with attachments and images
     - encode file in UTF-8
