Framework codjo.net
===================

This is a small tool to translate Confluence wiki content to Github wiki content.
To run the tool, you have to precise the following maven properties :
- confluencePrdUrl
- confluencePrdUser
- confluencePrdPassword


TODO :
 * deal with arguments : outputPath, main page name ...etc
 * Detail panel to implement (with library name, security support and plugin
 * {{pom.xml}} not traducted
 * insert news history and pull request in changelog
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
          See how to deal with framework versions

 * Decide wether or not we translate the header menu "project reports | svn | recherche | mantis"

 * automatic upload of wiki on github
     - create wiki repository
     - execute git clone git clone https://github.com/codjo/codjo-LIBRARY.wiki.git
     - launch TranslateTool
     - commit modifications in git
     - push new wiki
     NB : github API can't deal with wiki site


 * Reflexion on news process :
   - developper write an issue with label codjo-administration
   - try to understand link between issue and pull request --> during plateform meeting we close issues + pull request
   - when doing the stabilisation, create a new Label for the issue (cf github api and codjo plugin) with framework number
   -

 
 DONE :
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