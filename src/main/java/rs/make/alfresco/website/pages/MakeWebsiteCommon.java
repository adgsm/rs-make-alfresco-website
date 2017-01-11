package rs.make.alfresco.website.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Status;

import rs.make.alfresco.alfcontent.AlfContent;
import rs.make.alfresco.common.message.MakeMessage;
import rs.make.alfresco.common.webscripts.MakeCommonHelpers;

public class MakeWebsiteCommon {

	protected MakeCommonHelpers makeCommonHelpers;
	public MakeCommonHelpers getMakeCommonHelpers() {
		return makeCommonHelpers;
	}
	public void setMakeCommonHelpers( MakeCommonHelpers makeCommonHelpers ) {
		this.makeCommonHelpers = makeCommonHelpers;
	}

	protected SiteService siteService;
	public SiteService getSiteService() {
		return siteService;
	}
	public void setSiteService( SiteService siteService ) {
		this.siteService = siteService;
	}

	protected NamespaceService namespaceService;
	public NamespaceService getNamespaceService() {
		return namespaceService;
	}
	public void setNamespaceService( NamespaceService namespaceService ) {
		this.namespaceService = namespaceService;
	}

	protected NodeService nodeService;
	public NodeService getNodeService() {
		return nodeService;
	}
	public void setNodeService( NodeService nodeService ) {
		this.nodeService = nodeService;
	}

	protected FileFolderService fileFolderService;
	public FileFolderService getFileFolderService() {
		return fileFolderService;
	}
	public void setFileFolderService( FileFolderService fileFolderService ) {
		this.fileFolderService = fileFolderService;
	}

	private final String WEBSITE_SHARE_SITE_NAME = "website";
	private final String WEBSITE_SHARE_SITE_DOCUMENT_LIBRARY_NAME = "documentLibrary";

	private final String WEBSITE_PAGE_NAMESPACE_URI = "http://www.make.rs/model/website/1.0";
	private final String WEBSITE_PAGE_TYPE_NAME = "webSitePage";
	private final QName WEBSITE_PAGE_TYPE = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_TYPE_NAME );
	private final String WEBSITE_PAGE_ACTIVE_PROPERTY_NAME = "active";
	private final QName WEBSITE_PAGE_ACTIVE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_ACTIVE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_RELATED_PAGES_PROPERTY_NAME = "relatedPages";
	private final QName WEBSITE_PAGE_RELATED_PAGES_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_RELATED_PAGES_PROPERTY_NAME );
	private final String WEBSITE_PAGE_MENUS_PROPERTY_NAME = "menus";
	private final QName WEBSITE_PAGE_MENUS_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_MENUS_PROPERTY_NAME );
	private final String WEBSITE_ARCHIVED_ASPECT_NAME = "archived";
	private final QName ARCHIVED_CONTENT_ASPECT= QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_ARCHIVED_ASPECT_NAME );

	private static Logger logger = Logger.getLogger( MakeWebsiteCommon.class );

	public SiteInfo getWebsite( MakeMessage message ) throws Exception{
		SiteInfo website = null;
		boolean shareSiteExist = siteService.hasSite( WEBSITE_SHARE_SITE_NAME );
		if( !shareSiteExist ){
			ArrayList<String> args = new ArrayList<String>(1);
			args.add( WEBSITE_SHARE_SITE_NAME );
			String errorMessage = message.get( "error.invalidWebsiteShareSiteName" , args );
			throw new Exception( errorMessage , new Throwable( String.valueOf( Status.STATUS_BAD_REQUEST ) ) );
		}
		website = siteService.getSite( WEBSITE_SHARE_SITE_NAME );
		return website;
	}

	public NodeRef getWebsiteRootNode( MakeMessage message ) throws Exception{
		NodeRef rootNode = null;
		boolean shareSiteExist = siteService.hasSite( WEBSITE_SHARE_SITE_NAME );
		if( !shareSiteExist ){
			ArrayList<String> args = new ArrayList<String>(1);
			args.add( WEBSITE_SHARE_SITE_NAME );
			String errorMessage = message.get( "error.invalidWebsiteShareSiteName" , args );
			throw new Exception( errorMessage , new Throwable( String.valueOf( Status.STATUS_BAD_REQUEST ) ) );
		}
		rootNode = siteService.getContainer( WEBSITE_SHARE_SITE_NAME , WEBSITE_SHARE_SITE_DOCUMENT_LIBRARY_NAME );
		if( rootNode == null ){
			ArrayList<String> args = new ArrayList<String>(1);
			args.add( WEBSITE_SHARE_SITE_NAME );
			args.add( WEBSITE_SHARE_SITE_DOCUMENT_LIBRARY_NAME );
			String errorMessage = message.get( "error.invalidWebsiteShareDocumentLibraryName" , args );
			throw new Exception( errorMessage , new Throwable( String.valueOf( Status.STATUS_BAD_REQUEST ) ) );
		}
		return rootNode;
	}

	public String getRelatedPagesQuery( NodeRef relatedPagesNode ){
		String query = "";
		@SuppressWarnings("unchecked")
		ArrayList<Path> relatedPages = (ArrayList<Path>) nodeService.getProperty( relatedPagesNode , WEBSITE_PAGE_RELATED_PAGES_PROPERTY );
		if( relatedPages == null || relatedPages.size() == 0 ) return " AND FALSE";
		query += " AND ( ";
		for( int i=0; i<relatedPages.size(); i++ ){
			if( i > 0 ) query  += " OR ";
			Path relatedPage = relatedPages.get( i );
			query += "PATH:\"" + relatedPage.toPrefixString( namespaceService ) + "\"";
		}
		query += ") ";
		return query;
	}

	public String getDisplayPathQuery( String displayPath , String luceneKey , MakeMessage message ) throws Exception, FileNotFoundException{
		logger.debug( "Looking for a website page with display path \"" + displayPath + "\"." );
		NodeRef websiteRootNode = getWebsiteRootNode( message );
		if( displayPath.startsWith( "/" ) ) displayPath = displayPath.replaceFirst( "/" , "" );
		logger.debug( "Path relative to website root is \"" + displayPath + "\"." );
		List<String> pathElements = Arrays.asList( displayPath.split( Pattern.quote( "/" ) ) );
		try{
			FileInfo pageInfo = fileFolderService.resolveNamePath( websiteRootNode , pathElements );
			if( pageInfo.getType().equals( WEBSITE_PAGE_TYPE ) ){
				logger.debug( "\"" + pageInfo.getName() + "\" points to a webpage \"" + pageInfo.getName() + "\"." );
				return getSpecificPagesQuery( pageInfo.getNodeRef().toString() , luceneKey , message );
			}
			else{
				logger.debug( "\"" + pageInfo.getName() + "\" does not point to a webpage." );
			}
		}
		catch( FileNotFoundException fnfe ){
			logger.debug( "\"" + displayPath + "\" does not point to an existing item." );
		}
		return " AND FALSE";
	}

	public String getSpecificPagesQuery( String nodeRefsStr , String luceneKey , MakeMessage message ) throws Exception{
		String query = "";
		String[] nodeRefsArr = nodeRefsStr.split( Pattern.quote( "," ) );
		for( int i=0; i<nodeRefsArr.length; i++ ){
			String nodeRefStr = nodeRefsArr[ i ];
			if( AlfContent.isNodeRef( nodeRefStr ) || nodeRefStr.equals( "root" ) ){
				if( i > 0 ) query  += " OR ";
				query += "" + luceneKey + ":\"" + ( ( nodeRefStr.equals( "root" ) ) ? getWebsiteRootNode( message ).toString() : nodeRefStr ) + "\"";
			}
		}
		if( query.length() > 0 ) query = " AND ( " + query + ") ";
		return query;
	}

	public String getArchivedQuery( Boolean archived ){
		String query = "";
		if( archived.booleanValue() ){
			query += " +ASPECT:\"" + ARCHIVED_CONTENT_ASPECT.toPrefixString( namespaceService ) + "\" ";
		}
		else{
			query += " -ASPECT:\"" + ARCHIVED_CONTENT_ASPECT.toPrefixString( namespaceService ) + "\" ";
		}
		return query;
	}

	public String getActivePageQuery( boolean active ){
		String query = "";
		query += " +@" + MakeCommonHelpers.escapeLuceneQuery( WEBSITE_PAGE_ACTIVE_PROPERTY.toPrefixString( namespaceService ) ) + ":" + Boolean.toString( active ) + " ";
		return query;
	}

	public String getMenuPagesQuery( String menus ){
		String query = "";
		String[] menusArr = menus.split( Pattern.quote( "," ) );
		for( int i=0; i<menusArr.length; i++ ){
			String menu = menusArr[ i ];
			if( i > 0 ) query  += " OR ";
			query += " @" + MakeCommonHelpers.escapeLuceneQuery( WEBSITE_PAGE_MENUS_PROPERTY.toPrefixString( namespaceService ) ) + ":\"" + menu + "\" ";
		}
		if( query.length() > 0 ) query = " AND ( " + query + ") ";
		return query;
	}
}
