package rs.make.alfresco.website.pages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition.SortType;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.google.gson.Gson;

import rs.make.alfresco.common.message.MakeMessage;
import rs.make.alfresco.common.status.MakeStatus;
import rs.make.alfresco.common.webscripts.MakeCommonHelpers;

public class MakeWebsitePagesList extends DeclarativeWebScript {

	protected MakeStatus makeStatus;
	public MakeStatus getMakeStatus() {
		return makeStatus;
	}
	public void setMakeStatus( MakeStatus makeStatus ) {
		this.makeStatus = makeStatus;
	}

	protected MakeCommonHelpers makeCommonHelpers;
	public MakeCommonHelpers getMakeCommonHelpers() {
		return makeCommonHelpers;
	}
	public void setMakeCommonHelpers( MakeCommonHelpers makeCommonHelpers ) {
		this.makeCommonHelpers = makeCommonHelpers;
	}

	protected MakeWebsiteCommon makeWebsiteCommon;
	public MakeWebsiteCommon getMakeWebsiteCommon() {
		return makeWebsiteCommon;
	}
	public void setMakeWebsiteCommon( MakeWebsiteCommon makeWebsiteCommon ) {
		this.makeWebsiteCommon = makeWebsiteCommon;
	}

	protected SearchService searchService;
	public SearchService getSearchService() {
		return searchService;
	}
	public void setSearchService( SearchService searchService ) {
		this.searchService = searchService;
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

	protected TaggingService taggingService;
	public TaggingService getTaggingService() {
		return taggingService;
	}
	public void setTaggingService( TaggingService taggingService ) {
		this.taggingService = taggingService;
	}

	private final StoreRef STOREREF = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
	private final String WEBSITE_PAGE_NAMESPACE_URI = "http://www.make.rs/model/website/1.0";
	private final String WEBSITE_PAGE_TYPE_NAME = "webSitePage";
	private final QName WEBSITE_PAGE_TYPE = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_TYPE_NAME );
	private final String WEBSITE_PAGE_LINK_ASPECT_NAME = "webSiteLinkPage";
	private final QName WEBSITE_PAGE_LINK_ASPECT = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_LINK_ASPECT_NAME );
//	private final String WEBSITE_PAGE_NEWS_ASPECT_NAME = "newsPage";
//	private final QName WEBSITE_PAGE_NEWS_ASPECT = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_NEWS_ASPECT_NAME );
	private final String WEBSITE_PAGE_REAL_RELATIVE_PATH_PROPERTY_NAME = "realRelativePath";
	private final QName WEBSITE_PAGE_REAL_RELATIVE_PATH_PROPERTY= QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_REAL_RELATIVE_PATH_PROPERTY_NAME );
	private final String WEBSITE_PAGE_RELATIVE_PATH_PROPERTY_NAME = "relativePath";
	private final QName WEBSITE_PAGE_RELATIVE_PATH_PROPERTY= QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_RELATIVE_PATH_PROPERTY_NAME );
	private final String WEBSITE_PAGE_BREADCRUMBS_PROPERTY_NAME = "breadcrumbs";
	private final QName WEBSITE_PAGE_BREADCRUMBS_PROPERTY= QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_BREADCRUMBS_PROPERTY_NAME );
	private final String WEBSITE_PAGE_NATIVE_ORDER_PROPERTY_NAME = "nativeOrder";
	private final QName WEBSITE_PAGE_NATIVE_ORDER_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_NATIVE_ORDER_PROPERTY_NAME );
	private final String WEBSITE_PAGE_ASPECTS_NAME = "aspects";
	private final QName WEBSITE_PAGE_ASPECTS_PROPERTY= QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_ASPECTS_NAME );
	private final String WEBSITE_PAGE_LINK_ASPECT_LINK_TARGET_PROPERTY_NAME = "linkTarget";
	private final QName WEBSITE_PAGE_LINK_ASPECT_LINK_TARGET_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_LINK_ASPECT_LINK_TARGET_PROPERTY_NAME );
	private final String WEBSITE_PAGE_NEWS_ASPECT_DATE_PROPERTY_NAME = "date";
	private final QName WEBSITE_PAGE_NEWS_ASPECT_DATE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_NEWS_ASPECT_DATE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_PARENT_PROPERTY_NAME = "parent";
	private final QName WEBSITE_PAGE_PARENT_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_PARENT_PROPERTY_NAME );
	private final String WEBSITE_PAGE_TAGS_PROPERTY_NAME = "tags";
	private final QName WEBSITE_PAGE_TAGS_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_TAGS_PROPERTY_NAME );
	private final String WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY_NAME = "calendarEntry";
	private final QName WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY_NAME );
	private final String CALENDAR_TITLE_PROPERTY_NAME = "eventTitle";
	private final QName CALENDAR_TITLE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , CALENDAR_TITLE_PROPERTY_NAME );
	private final String CALENDAR_DESCRIPTION_PROPERTY_NAME = "eventDescription";
	private final QName CALENDAR_DESCRIPTION_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , CALENDAR_DESCRIPTION_PROPERTY_NAME );
	private final String CALENDAR_LOCATION_PROPERTY_NAME = "eventLocation";
	private final QName CALENDAR_LOCATION_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , CALENDAR_LOCATION_PROPERTY_NAME );
	private final String CALENDAR_START_DATE_PROPERTY_NAME = "eventStartDate";
	private final QName CALENDAR_START_DATE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , CALENDAR_START_DATE_PROPERTY_NAME );
	private final String CALENDAR_END_DATE_PROPERTY_NAME = "eventEndDate";
	private final QName CALENDAR_END_DATE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , CALENDAR_END_DATE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_DISPLAY_TITLE_PROPERTY_NAME = "displayTitle";
	private final QName WEBSITE_PAGE_DISPLAY_TITLE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_DISPLAY_TITLE_PROPERTY_NAME );

	private final String LUCENE_ID_KEY = "ID";
	private final String LUCENE_PRIMARY_PARENT_KEY = "PRIMARYPARENT";

	private final String FILE_NAME_KEY = "name";
	private final String FILE_TITLE_KEY = "title";

	private final String REQUEST_TERM_KEY = "term";
	private final String REQUEST_RELATED_PAGES_KEY = "related-pages";
	private final String REQUEST_DISPLAY_PATH_KEY = "querypath";
	private final String REQUEST_NODEREFS_KEY = "nodeRef";
	private final String REQUEST_PARENT_NODEREFS_KEY = "parent";
	private final String REQUEST_ARCHIVED_KEY = "archived";
	private final String REQUEST_ACTIVE_KEY = "active";
	private final String REQUEST_MENUS_KEY = "menus";
	private final String REQUEST_NATIVE_ORDER_KEY = "native-order";
	private final String REQUEST_NEWS_OLDER_THEN_KEY = "news-older-then";
	private final String REQUEST_NEWS_YOUNGER_THEN_KEY = "news-younger-then";
	private final String REQUEST_ORDER_COMPARISON_KEY = "order-comparison";
	private final String REQUEST_TAGS_KEY = "tags";
	private final String REQUEST_WITHOUT_TAGS_KEY = "without-tags";
	private final String REQUEST_WITH_ASPECTS_KEY = "with-aspects";
	private final String REQUEST_WITHOUT_ASPECTS_KEY = "without-aspects";
	private final String REQUEST_SORT_BY_KEY = "sort-by";
	private final String REQUEST_SORT_DIR_KEY = "sort-dir";

	private final int MAX_PERMISSION_CHECKS = 10000;

	private boolean statusThrown = false;

	private static Logger logger = Logger.getLogger( MakeWebsitePagesList.class );

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Object> executeImpl( WebScriptRequest req , Status status , Cache cache ) {
		Map<String, Object> model = new HashMap<String, Object>();
		try{
			WebScript webscript = req.getServiceMatch().getWebScript();
			MakeMessage message = new MakeMessage( webscript );

			String authenticatedUserName = AuthenticationUtil.getFullyAuthenticatedUser();
			if( authenticatedUserName == null ){
				authenticatedUserName = AuthenticationUtil.getGuestUserName();
				AuthenticationUtil.setRunAsUser( authenticatedUserName );
			}

			JSONArray entries = new JSONArray();
			ResultSet results = executeQuery( req , status , cache , message );

			AuthenticationUtil.setRunAsUserSystem();

			NodeRef websiteRootNode = makeWebsiteCommon.getWebsiteRootNode( message );
			for( NodeRef result : results.getNodeRefs() ){
				entries.add( prepareProperty( result , websiteRootNode , message ) );
				logger.debug( "[" + MakeWebsitePagesList.class.getName() + "] \"" + nodeService.getProperty( result , ContentModel.PROP_NAME ).toString() + " added to pages response." );
			}

			AuthenticationUtil.setRunAsUser( authenticatedUserName );

			Gson gson = new Gson();
			model.put( "response", gson.toJson( entries ) );
			String parsedMessage = message.get( "success.text" , null );
			model.put( "success", ( parsedMessage != null ) ? parsedMessage : "" );
		}
		catch( Exception e ) {
			if( !statusThrown ) makeStatus.throwStatus( e.getMessage() , status , Status.STATUS_INTERNAL_SERVER_ERROR , MakeWebsitePagesList.class );
			return null;
		}
		return model;
	}

	private ResultSet executeQuery( WebScriptRequest req , Status status , Cache cache , MakeMessage message ) throws Exception {
		SearchParameters searchParameters = new SearchParameters();
		searchParameters.addStore( STOREREF );
		searchParameters.setLanguage( SearchService.LANGUAGE_LUCENE );

		int skip = makeCommonHelpers.getSkip( req , message , status , -1 );
		int limit = makeCommonHelpers.getLimit( req , message , status , -1 );
		if( skip != -1 ) searchParameters.setSkipCount( skip );
		if( limit != -1 ) {
			searchParameters.setLimitBy( LimitBy.FINAL_SIZE );
			searchParameters.setLimit( limit );
			searchParameters.setMaxItems( limit );
			searchParameters.setMaxPermissionChecks( MAX_PERMISSION_CHECKS );
		}
		String term = makeCommonHelpers.getString( req , REQUEST_TERM_KEY , message , status , false );
		NodeRef relatedPagesNode = makeCommonHelpers.getNode( req , REQUEST_RELATED_PAGES_KEY , message , status , false );
		String nodeRefsStr = makeCommonHelpers.getString( req , REQUEST_NODEREFS_KEY , message , status , false );
		String dispayPathStr = makeCommonHelpers.getString( req , REQUEST_DISPLAY_PATH_KEY , message , status , false );
		String parentNodeRefsStr = makeCommonHelpers.getString( req , REQUEST_PARENT_NODEREFS_KEY , message , status , false );
		Boolean archived = makeCommonHelpers.getBooleanObj( req , REQUEST_ARCHIVED_KEY , message , status , false );
		Boolean active = makeCommonHelpers.getBooleanObj( req , REQUEST_ACTIVE_KEY , message , status , false );
		String menus = makeCommonHelpers.getString( req , REQUEST_MENUS_KEY , message , status , false );
		long nativeOrder = makeCommonHelpers.getLong( req , REQUEST_NATIVE_ORDER_KEY , message , status , false );
		long newsOlderThen = makeCommonHelpers.getLong( req , REQUEST_NEWS_OLDER_THEN_KEY , message , status , false );
		long newsYoungerThen = makeCommonHelpers.getLong( req , REQUEST_NEWS_YOUNGER_THEN_KEY , message , status , false );
		String orderComparison = makeCommonHelpers.getString( req , REQUEST_ORDER_COMPARISON_KEY , message , status , false );
		ArrayList<String> tags = makeCommonHelpers.getStrings( req , REQUEST_TAGS_KEY , message , status , false );
		ArrayList<String> withoutTags = makeCommonHelpers.getStrings( req , REQUEST_WITHOUT_TAGS_KEY , message , status , false );
		ArrayList<QName> withAspects = makeCommonHelpers.getQNames( req , REQUEST_WITH_ASPECTS_KEY , message , status , false );
		ArrayList<QName> withoutAspects = makeCommonHelpers.getQNames( req , REQUEST_WITHOUT_ASPECTS_KEY , message , status , false );
		String sortBy = makeCommonHelpers.getString( req , REQUEST_SORT_BY_KEY , message , status , false );
		Boolean sortDir = makeCommonHelpers.getBooleanObj( req , REQUEST_SORT_DIR_KEY , message , status , false );
		
		String query = "+TYPE:\"" + WEBSITE_PAGE_TYPE.toPrefixString( namespaceService ) + "\"";
		if( term != null ) {
			ArrayList<QName> searchThrough = new ArrayList<QName>(3);
			searchThrough.add( WEBSITE_PAGE_DISPLAY_TITLE_PROPERTY );
			searchThrough.add( ContentModel.PROP_TITLE );
			searchThrough.add( ContentModel.PROP_DESCRIPTION );
			query += makeCommonHelpers.getMetadataContainsQuery( term , searchThrough );
		}
		if( relatedPagesNode != null ) query += makeWebsiteCommon.getRelatedPagesQuery( relatedPagesNode );
		if( nodeRefsStr != null ) query += makeWebsiteCommon.getSpecificPagesQuery( nodeRefsStr , LUCENE_ID_KEY , message );
		if( dispayPathStr != null ) query += makeWebsiteCommon.getDisplayPathQuery( dispayPathStr , LUCENE_ID_KEY , message );
		if( parentNodeRefsStr != null ) query += makeWebsiteCommon.getSpecificPagesQuery( parentNodeRefsStr , LUCENE_PRIMARY_PARENT_KEY , message );
		if( archived != null ) query += makeWebsiteCommon.getArchivedQuery( archived );
		if( active != null ) query += makeWebsiteCommon.getActivePageQuery( active.booleanValue() );
		if( menus != null ) query += makeWebsiteCommon.getMenuPagesQuery( menus );
		if( nativeOrder > -1 ) query += makeCommonHelpers.getNativeOrderComparisonQuery( nativeOrder , orderComparison , WEBSITE_PAGE_NATIVE_ORDER_PROPERTY );
		if( tags != null ) query += makeCommonHelpers.getTagsQuery( tags );
		if( withoutTags != null ) query += makeCommonHelpers.getTagsQuery( withoutTags , true , true );
		Long newOlderThenObj = ( newsOlderThen > -1 ) ? new Long( newsOlderThen ) : null;
		Long newYoungerThenObj = ( newsYoungerThen > -1 ) ? new Long( newsYoungerThen ) : null;
		if( newsOlderThen > -1 || newsYoungerThen > -1 ) query += makeCommonHelpers.getDateQuery( newOlderThenObj , newYoungerThenObj , "gte" , "lte" , WEBSITE_PAGE_NEWS_ASPECT_DATE_PROPERTY );
		if( nativeOrder > -1 ) query += makeCommonHelpers.getNativeOrderComparisonQuery( nativeOrder , orderComparison , WEBSITE_PAGE_NATIVE_ORDER_PROPERTY );
		if( withAspects != null ) query += makeCommonHelpers.getAspectQuery( withAspects , true );
		if( withoutAspects != null ) query += makeCommonHelpers.getAspectQuery( withoutAspects , false );

		String sortField = ContentModel.PROP_NAME.toPrefixString( namespaceService );
		if( sortBy != null ){
			try{
				sortField = QName.createQName( sortBy ).toPrefixString( namespaceService );
			}
			catch( Exception e ){
				logger.error( "[" + MakeWebsitePagesList.class.getName() + "] Exception when trying to resolve provided sort field QName: \"" + e.getMessage() + "\"." );
			}
		}
		boolean bSortDir = true;
		if( sortDir != null ){
			bSortDir = sortDir.booleanValue();
		}
		SortDefinition sortDefinition = new SortDefinition( SortType.FIELD , sortField , bSortDir );
		searchParameters.addSort( sortDefinition );

		logger.debug( "[" + MakeWebsitePagesList.class.getName() + "] Pages sort: \"" + sortDefinition.getField() + ", " + sortDefinition.getSortType().name() + ", " + sortDefinition.isAscending() + "\"." );

		logger.debug( "[" + MakeWebsitePagesList.class.getName() + "] Pages query: \"" + query + "\"." );

		searchParameters.setQuery( query );
		ResultSet results = searchService.query( searchParameters );

		return results;
	}

	@SuppressWarnings("unchecked")
	private Map<QName,Serializable> prepareProperty( NodeRef result , NodeRef websiteRootNode , MakeMessage message ) throws Exception{
		Map<QName,Serializable> properties = nodeService.getProperties( result );

		// prepare relative path key
		List<FileInfo> relativePathList = null;
		List<String> realRelativePathList = fileFolderService.getNameOnlyPath( websiteRootNode , result );
		if( nodeService.hasAspect( result , WEBSITE_PAGE_LINK_ASPECT ) ){
			Path link = (Path) nodeService.getProperty( result , WEBSITE_PAGE_LINK_ASPECT_LINK_TARGET_PROPERTY );
			NodeRef rootNode = nodeService.getRootNode( StoreRef.STORE_REF_WORKSPACE_SPACESSTORE );
			List<NodeRef> linkNodes = searchService.selectNodes( rootNode , link.toPrefixString( namespaceService ) , null , namespaceService , true , SearchService.LANGUAGE_XPATH );
			NodeRef linkNode = linkNodes.get(0);
			relativePathList = fileFolderService.getNamePath( websiteRootNode , linkNode );
		}
		else{
			relativePathList = fileFolderService.getNamePath( websiteRootNode , result );
		}

		// prepare breadcrumbs
		String realRelativePath = "/" + String.join( "/" , realRelativePathList );
		String relativePath = "";
		JSONArray breadcrumbs = new JSONArray();
		for( FileInfo fileInfo : relativePathList ){
			String fileName = fileInfo.getName();
			String fileTitle = (String) fileInfo.getProperties().get( ContentModel.PROP_TITLE );
			JSONObject breadcrumb = new JSONObject();
			breadcrumb.put( FILE_NAME_KEY , fileName );
			breadcrumb.put( FILE_TITLE_KEY , fileTitle );
			relativePath += "/" + fileInfo.getName();
			breadcrumbs.add( breadcrumb );
		}

		// prepare node aspects response
		HashSet<QName> aspects = (HashSet<QName>) nodeService.getAspects( result );
		// prepare node paren response
		NodeRef parent = nodeService.getPrimaryParent( result ).getParentRef();

		properties.put( WEBSITE_PAGE_ASPECTS_PROPERTY , aspects );
		properties.put( WEBSITE_PAGE_REAL_RELATIVE_PATH_PROPERTY , realRelativePath );
		properties.put( WEBSITE_PAGE_RELATIVE_PATH_PROPERTY , relativePath );
		properties.put( WEBSITE_PAGE_BREADCRUMBS_PROPERTY , breadcrumbs );
		properties.put( WEBSITE_PAGE_PARENT_PROPERTY , parent );

		// convert nativeOrder type from date to long
		Date nativeOrder = (Date) nodeService.getProperty( result , WEBSITE_PAGE_NATIVE_ORDER_PROPERTY );
		if( nativeOrder != null ){
			// override default Date to String for nativeOrder (make it long)
			long nativeOrderLong = nativeOrder.getTime();
			properties.put( WEBSITE_PAGE_NATIVE_ORDER_PROPERTY , nativeOrderLong );
		}

		//prepare node tags response
		List<String> tags = taggingService.getTags( result );
		properties.put( WEBSITE_PAGE_TAGS_PROPERTY , tags.toArray() );

		// prepare calendar entry response
		String calendarEntryName = (String) nodeService.getProperty( result , WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY );
		if( calendarEntryName != null ) {
			SiteInfo website = makeWebsiteCommon.getWebsite( message );
			CalendarEntry calendarEntry = makeCommonHelpers.getCalendarEntry( website , calendarEntryName );
			if( calendarEntry != null ){
				properties.put( CALENDAR_TITLE_PROPERTY , calendarEntry.getTitle() );
				properties.put( CALENDAR_DESCRIPTION_PROPERTY , calendarEntry.getDescription() );
				properties.put( CALENDAR_LOCATION_PROPERTY , calendarEntry.getLocation() );
				properties.put( CALENDAR_START_DATE_PROPERTY , calendarEntry.getStart() );
				properties.put( CALENDAR_END_DATE_PROPERTY , calendarEntry.getEnd() );
			}
		}

		return properties;
	}
}
