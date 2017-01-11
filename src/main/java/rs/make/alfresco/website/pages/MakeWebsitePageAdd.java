package rs.make.alfresco.website.pages;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.google.gson.Gson;

import org.alfresco.service.namespace.QName;

import rs.make.alfresco.alfcontent.AlfContent;
import rs.make.alfresco.common.locale.MakeLocale;
import rs.make.alfresco.common.message.MakeMessage;
import rs.make.alfresco.common.status.MakeStatus;
import rs.make.alfresco.common.webscripts.MakeCommonHelpers;

public class MakeWebsitePageAdd extends DeclarativeWebScript {

	protected AlfContent alfContent;
	public AlfContent getAlfContent() {
		return alfContent;
	}
	public void setAlfContent( AlfContent alfContent ) {
		this.alfContent = alfContent;
	}

	protected MakeStatus makeStatus;
	public MakeStatus getMakeStatus() {
		return makeStatus;
	}
	public void setMakeStatus( MakeStatus makeStatus ) {
		this.makeStatus = makeStatus;
	}

	protected MakeLocale makeLocale;
	public MakeLocale getMakeLocale() {
		return makeLocale;
	}
	public void setMakeLocale( MakeLocale makeLocale ) {
		this.makeLocale = makeLocale;
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

	private final String WEBSITE_ADMIN_AUTHORITY = "website-admins";

	private final String WEBSITE_PAGE_NAMESPACE_URI = "http://www.make.rs/model/website/1.0";
	private final String WEBSITE_PAGE_TYPE_NAME = "webSitePage";
	private final String WEBSITE_PAGE_CONTENT_TYPE_NAME = "webSitePageContent";
	private final QName WEBSITE_PAGE_TYPE = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_TYPE_NAME );
	private final String WEBSITE_PAGE_GROUP_ASPECT_NAME = "webSiteGroupPage";
	private final QName WEBSITE_PAGE_GROUP_ASPECT = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_GROUP_ASPECT_NAME );
	private final String WEBSITE_PAGE_LINK_ASPECT_NAME = "webSiteLinkPage";
	private final QName WEBSITE_PAGE_LINK_ASPECT = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_LINK_ASPECT_NAME );
	private final String WEBSITE_PAGE_NEWS_ASPECT_NAME = "newsPage";
	private final QName WEBSITE_PAGE_NEWS_ASPECT = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_NEWS_ASPECT_NAME );
	private final String WEBSITE_PAGE_EVENT_ASPECT_NAME = "eventPage";
	private final QName WEBSITE_PAGE_EVENT_ASPECT = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_EVENT_ASPECT_NAME );
	private final String WEBSITE_PAGE_LOCALE_PROPERTY_NAME = "locale";
	private final QName WEBSITE_PAGE_LOCALE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_LOCALE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_DISPLAY_TITLE_PROPERTY_NAME = "displayTitle";
	private final QName WEBSITE_PAGE_DISPLAY_TITLE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_DISPLAY_TITLE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_DISPLAY_SUBTITLE_PROPERTY_NAME = "displaySubtitle";
	private final QName WEBSITE_PAGE_DISPLAY_SUBTITLE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_DISPLAY_SUBTITLE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_TEMPLATE_PROPERTY_NAME = "template";
	private final QName WEBSITE_PAGE_TEMPLATE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_TEMPLATE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_ICON_PROPERTY_NAME = "icon";
	private final QName WEBSITE_PAGE_ICON_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_ICON_PROPERTY_NAME );
	private final String WEBSITE_PAGE_SKELETON_IMAGES_PROPERTY_NAME = "skeletonImages";
	private final QName WEBSITE_PAGE_SKELETON_IMAGES_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_SKELETON_IMAGES_PROPERTY_NAME );
	private final String WEBSITE_PAGE_CONTAINERS_PROPERTY_NAME = "containers";
	private final QName WEBSITE_PAGE_CONTAINERS_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_CONTAINERS_PROPERTY_NAME );
	private final String WEBSITE_PAGE_NATIVE_ORDER_PROPERTY_NAME = "nativeOrder";
	private final QName WEBSITE_PAGE_NATIVE_ORDER_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_NATIVE_ORDER_PROPERTY_NAME );
	private final String WEBSITE_PAGE_ACTIVE_PROPERTY_NAME = "active";
	private final QName WEBSITE_PAGE_ACTIVE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_ACTIVE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_RELATED_PAGES_PROPERTY_NAME = "relatedPages";
	private final QName WEBSITE_PAGE_RELATED_PAGES_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_RELATED_PAGES_PROPERTY_NAME );
	private final String WEBSITE_PAGE_MENUS_PROPERTY_NAME = "menus";
	private final QName WEBSITE_PAGE_MENUS_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_MENUS_PROPERTY_NAME );
	private final String WEBSITE_PAGE_LINK_ASPECT_LINK_TARGET_PROPERTY_NAME = "linkTarget";
	private final QName WEBSITE_PAGE_LINK_ASPECT_LINK_TARGET_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_LINK_ASPECT_LINK_TARGET_PROPERTY_NAME );
	private final String WEBSITE_PAGE_NEWS_ASPECT_DATE_PROPERTY_NAME = "date";
	private final QName WEBSITE_PAGE_NEWS_ASPECT_DATE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_NEWS_ASPECT_DATE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_EVENT_ASPECT_DATE_PROPERTY_NAME = "date";
	private final QName WEBSITE_PAGE_EVENT_ASPECT_DATE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_EVENT_ASPECT_DATE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_EVENT_ASPECT_SHOW_REGISTRATION_FORM_PROPERTY_NAME = "showRegistrationForm";
	private final QName WEBSITE_PAGE_EVENT_ASPECT_SHOW_REGISTRATION_FORM_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_EVENT_ASPECT_SHOW_REGISTRATION_FORM_PROPERTY_NAME );
	private final String WEBSITE_PAGE_EVENT_ASPECT_REGISTRATION_FORM_TEMPLATE_PROPERTY_NAME = "registrationFormTemplate";
	private final QName WEBSITE_PAGE_EVENT_ASPECT_REGISTRATION_FORM_TEMPLATE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_EVENT_ASPECT_REGISTRATION_FORM_TEMPLATE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_EVENT_ASPECT_REGISTRATION_CODE_PROPERTY_NAME = "registrationCode";
	private final QName WEBSITE_PAGE_EVENT_ASPECT_REGISTRATION_CODE_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_EVENT_ASPECT_REGISTRATION_CODE_PROPERTY_NAME );
	private final String WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY_NAME = "calendarEntry";
	private final QName WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY_NAME );

	private final String JSON_PARENT_KEY = "parent";
	private final String JSON_NAME_KEY = "name";
	private final String JSON_TITLE_KEY = "title";
	private final String JSON_DESCRIPTION_KEY = "description";
	private final String JSON_DISPLAY_TITLE_KEY = "display-title";
	private final String JSON_DISPLAY_SUBTITLE_KEY = "display-subtitle";
	private final String JSON_TEMPLATE_KEY = "template";
	private final String JSON_ICON_KEY = "icon";
	private final String JSON_SKELETON_IMAGES_KEY = "skeletonImages";
	private final String JSON_CONTAINERS_KEY = "containers";
	private final String JSON_NATIVE_ORDER_KEY = "nativeOrder";
	private final String JSON_ACTIVE_KEY = "active";
	private final String JSON_RELATED_PAGES_KEY = "relatedPages";
	private final String JSON_MENUS_KEY = "menus";
	private final String JSON_STRICT_MODE_KEY = "strict-mode";
	private final String JSON_GROUPING_ITEM_KEY = "grouping-item";
	private final String JSON_LINK_ITEM_KEY = "link-item";
	private final String JSON_LINK_ITEM_VALUE_KEY = "link-item-value";
	private final String JSON_TAGS_KEY = "tags";
	private final String JSON_COUNTRY_TAGS_KEY = "country-tags";
	private final String JSON_SECTOR_TAGS_KEY = "sector-tags";
	private final String JSON_NEWS_PAGE_KEY = "news-page";
	private final String JSON_NEWS_PAGE_DATE_KEY = "news-page-date";
	private final String JSON_EVENT_PAGE_KEY = "event-page";
	private final String JSON_EVENT_PAGE_START_DATE_KEY = "event-page-start-date";
	private final String JSON_EVENT_PAGE_END_DATE_KEY = "event-page-end-date";
	private final String JSON_EVENT_PAGE_LOCATION_KEY = "event-page-location";
	private final String JSON_EVENT_PAGE_SHOW_REGISTRATION_FORM_KEY = "event-page-show-registration-form";
	private final String JSON_EVENT_PAGE_REGISTRATION_FORM_TEMPLATE_KEY = "event-page-registration-form-template";
	private final String JSON_REGISTRATION_CODE_KEY = "event-page-registration-code";

	private final String CALENDAR_TITLE_PROPERTY_NAME = "eventTitle";
	private final String CALENDAR_DESCRIPTION_PROPERTY_NAME = "eventDescription";
	private final String CALENDAR_LOCATION_PROPERTY_NAME = "eventLocation";
	private final String CALENDAR_START_DATE_PROPERTY_NAME = "eventStartDate";
	private final String CALENDAR_END_DATE_PROPERTY_NAME = "eventEndDate";

	private final String RESPONSE_NODEREF_KEY = "nodeRef";
	private final String RESPONSE_DISPLAY_PATH_KEY = "path";

	private final String CONTAINER_METHOD_KEY = "method";
	private final String CONTAINER_NAME_KEY = "container";
	private final String CONTAINER_SHOW_CONTENT_VAL = "showContent";
	
	private final String COUNTRY_TAG_PREFIX = "country--";
	private final String SECTOR_TAG_PREFIX = "sector--";

	private int responseThrowStatus = Status.STATUS_INTERNAL_SERVER_ERROR;

	private static Logger logger = Logger.getLogger( MakeWebsitePageAdd.class );

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Object> executeImpl( WebScriptRequest req , Status status , Cache cache ) {
		Map<String, Object> model = new HashMap<String, Object>();
		try{
			WebScript webscript = req.getServiceMatch().getWebScript();
			MakeMessage message = new MakeMessage( webscript );

			String authenticatedUserName = AuthenticationUtil.getFullyAuthenticatedUser();
			boolean userAuthorized = makeCommonHelpers.userIsSectionAdmin( authenticatedUserName , WEBSITE_ADMIN_AUTHORITY );
			if( !userAuthorized ){
				ArrayList<String> args = new ArrayList<String>(1);
				args.add( authenticatedUserName );
				String errorMessage = message.get( "error.unauthorized" , args );
				throw new Exception( errorMessage , new Throwable( String.valueOf( Status.STATUS_FORBIDDEN ) ) );
			}

			AuthenticationUtil.setRunAsUserSystem();

			NodeRef added = addPage( req , status , cache , message , authenticatedUserName );
			logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] \"" + nodeService.getProperty( added , ContentModel.PROP_NAME ).toString() + " page created." );

			JSONObject addedProperties = new JSONObject();
			addedProperties.put( RESPONSE_NODEREF_KEY , added.toString() );
			NodeRef websiteRootNode = makeWebsiteCommon.getWebsiteRootNode( message );
			List<String> arrPath = fileFolderService.getNameOnlyPath( websiteRootNode, added );
			addedProperties.put( RESPONSE_DISPLAY_PATH_KEY , new String( "/" ).concat( String.join( "/" , arrPath ) ) );

			Gson gson = new Gson();
			model.put( "response", gson.toJson( addedProperties ) );
			String parsedMessage = message.get( "success.text" , null );
			model.put( "success", ( parsedMessage != null ) ? parsedMessage : "" );

			AuthenticationUtil.setRunAsUser( authenticatedUserName );
		}
		catch( Exception e ) {
			try{
				logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Error message" + e.getMessage() );
				logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Error cause" + ( ( e.getCause() != null ) ? e.getCause().getMessage() : "" ) );
				responseThrowStatus = ( e.getCause() != null ) ? Integer.parseInt( e.getCause().getMessage() , 10 ) : Status.STATUS_INTERNAL_SERVER_ERROR;
			}
			catch( Exception rtse ){
				logger.error( "[" + MakeWebsitePageAdd.class.getName() + "] " + rtse.getMessage() );
			}
			logger.error( "[" + MakeWebsitePageAdd.class.getName() + "] " + e.getMessage() );
			makeStatus.throwStatus( e.getMessage() , status , responseThrowStatus , MakeWebsitePageAdd.class );
			return null;
		}
		return model;
	}

	private NodeRef addPage( WebScriptRequest req , Status status , Cache cache , MakeMessage message , String author ) throws Exception {
		NodeRef entry = null;
		Map<QName,Serializable> properties = new HashMap<QName,Serializable>();

		JSONObject requestJSON = makeCommonHelpers.validateJSONRequest( req , message , status );
		NodeRef parent = makeCommonHelpers.getNode( requestJSON , JSON_PARENT_KEY , message , status , false );
		if( parent == null ) parent = makeWebsiteCommon.getWebsiteRootNode( message );
		String name = makeCommonHelpers.getString( requestJSON , JSON_NAME_KEY , message , status , true );
		String title = makeCommonHelpers.getString( requestJSON , JSON_TITLE_KEY , message , status , false );
		String description = makeCommonHelpers.getString( requestJSON , JSON_DESCRIPTION_KEY , message , status , false );
		Locale locale = makeLocale.get( requestJSON );
		String displayTitle = makeCommonHelpers.getString( requestJSON , JSON_DISPLAY_TITLE_KEY , message , status , false );
		String displaySubtitle = makeCommonHelpers.getString( requestJSON , JSON_DISPLAY_SUBTITLE_KEY , message , status , false );
		String template = makeCommonHelpers.getString( requestJSON , JSON_TEMPLATE_KEY , message , status , true );
		String icon = makeCommonHelpers.getString( requestJSON , JSON_ICON_KEY , message , status , false );
		ArrayList<Path> skeletonImages = makeCommonHelpers.getPaths( requestJSON , JSON_SKELETON_IMAGES_KEY , message , status , false );
		JSONArray containers = makeCommonHelpers.getJSONArray( requestJSON , JSON_CONTAINERS_KEY , message , status , false );
		long nativeOrder = makeCommonHelpers.getNativeLong( requestJSON , JSON_NATIVE_ORDER_KEY , message , status , false );
		if( nativeOrder > -1 ) makeCommonHelpers.updateNativeOrder( nativeOrder , WEBSITE_PAGE_TYPE , WEBSITE_PAGE_NATIVE_ORDER_PROPERTY );
		boolean active = makeCommonHelpers.getBoolean( requestJSON , JSON_ACTIVE_KEY , message , status );
		ArrayList<Path> relatedPages = makeCommonHelpers.getPaths( requestJSON , JSON_RELATED_PAGES_KEY , message , status , false );
		ArrayList<String> menus = makeCommonHelpers.getStrings( requestJSON , JSON_MENUS_KEY , message , status , false );
		boolean strictMode = makeCommonHelpers.getBoolean( requestJSON , JSON_STRICT_MODE_KEY , message , status );
		Boolean groupingItem = makeCommonHelpers.getBooleanObj( requestJSON , JSON_GROUPING_ITEM_KEY , message , status , false );
		Boolean linkItem = makeCommonHelpers.getBooleanObj( requestJSON , JSON_LINK_ITEM_KEY , message , status , false );
		Boolean newsPage = makeCommonHelpers.getBooleanObj( requestJSON , JSON_NEWS_PAGE_KEY , message , status , false );
		Boolean eventPage = makeCommonHelpers.getBooleanObj( requestJSON , JSON_EVENT_PAGE_KEY , message , status , false );
		JSONArray tags = makeCommonHelpers.getJSONArray( requestJSON , JSON_TAGS_KEY , message , status , false );
		JSONArray countryTags = makeCommonHelpers.getJSONArray( requestJSON , JSON_COUNTRY_TAGS_KEY , message , status , false );
		JSONArray sectorTags = makeCommonHelpers.getJSONArray( requestJSON , JSON_SECTOR_TAGS_KEY , message , status , false );

		properties.put( ContentModel.PROP_NAME , name );
		if( title != null || ( title == null && strictMode ) )
			properties.put( ContentModel.PROP_TITLE , title );
		if( description != null || ( description == null && strictMode ) )
			properties.put( ContentModel.PROP_DESCRIPTION , description );
		properties.put( ContentModel.PROP_AUTHOR , author );
		properties.put( WEBSITE_PAGE_LOCALE_PROPERTY , locale );
		if( displayTitle != null || ( displayTitle == null && strictMode ) )
			properties.put( WEBSITE_PAGE_DISPLAY_TITLE_PROPERTY , displayTitle );
		if( displaySubtitle != null || ( displaySubtitle == null && strictMode ) )
			properties.put( WEBSITE_PAGE_DISPLAY_SUBTITLE_PROPERTY , displaySubtitle );
		properties.put( WEBSITE_PAGE_TEMPLATE_PROPERTY , template );
		if( icon != null || ( icon == null && strictMode )  )
			properties.put( WEBSITE_PAGE_ICON_PROPERTY , icon );
		if( skeletonImages != null || ( skeletonImages == null && strictMode )  )
			properties.put( WEBSITE_PAGE_SKELETON_IMAGES_PROPERTY , skeletonImages );
		if( containers != null || ( containers == null && strictMode )  )
			properties.put( WEBSITE_PAGE_CONTAINERS_PROPERTY , containers );
		if( nativeOrder > -1 || ( nativeOrder == -1 && strictMode ) )
			properties.put( WEBSITE_PAGE_NATIVE_ORDER_PROPERTY , ( ( nativeOrder > -1 ) ? new Date( nativeOrder ) : new Date(0) ) );
		properties.put( WEBSITE_PAGE_ACTIVE_PROPERTY , active );
		if( relatedPages != null || ( relatedPages == null && strictMode )  ) properties.put( WEBSITE_PAGE_RELATED_PAGES_PROPERTY , relatedPages );
		if( menus != null || ( menus == null && strictMode )  ) properties.put( WEBSITE_PAGE_MENUS_PROPERTY , menus );

		entry = alfContent.createFolderNode( parent , name , WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_TYPE_NAME , properties );
		logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Added \"" + name + "\" webpage." );

		if( tags != null ){
			makeCommonHelpers.addTags( entry , tags , new Boolean( true ) );
		}
		if( countryTags != null ){
			makeCommonHelpers.addTags( entry , countryTags , new Boolean( true ) , COUNTRY_TAG_PREFIX );
		}
		if( sectorTags != null ){
			makeCommonHelpers.addTags( entry , sectorTags , new Boolean( true ) , SECTOR_TAG_PREFIX );
		}

		// set static contents
		ArrayList<NodeRef> staticContents = createStaticContents( entry , requestJSON , message , status );
		logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Added \"" + staticContents.size() + "\" webpage static contents." );

		// check if native order is not set
		Date nativeOrderDate = (Date) nodeService.getProperty( entry , WEBSITE_PAGE_NATIVE_ORDER_PROPERTY );
		if( nativeOrderDate == null ) nodeService.setProperty( entry , WEBSITE_PAGE_NATIVE_ORDER_PROPERTY , new Date() );

		// grouping item
		if( groupingItem != null ){
			logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Grouping item has \"" + groupingItem.booleanValue() + "\" value." );
			if( groupingItem.booleanValue() == true ){
				if( !nodeService.hasAspect( entry , WEBSITE_PAGE_GROUP_ASPECT ) ) nodeService.addAspect( entry , WEBSITE_PAGE_GROUP_ASPECT , null );
				logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Added grouping aspect to \"" + name + "\" web page." );
			}
			else{
				if( nodeService.hasAspect( entry , WEBSITE_PAGE_GROUP_ASPECT ) ) nodeService.removeAspect( entry , WEBSITE_PAGE_GROUP_ASPECT );
				logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Removed grouping aspect from \"" + name + "\" web page." );
			}
		}

		// link item
		if( linkItem != null ){
			logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Link item has \"" + linkItem.booleanValue() + "\" value." );
			if( nodeService.hasAspect( entry , WEBSITE_PAGE_LINK_ASPECT ) ) nodeService.removeAspect( entry , WEBSITE_PAGE_LINK_ASPECT );
			logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Removed link aspect from \"" + name + "\" web page." );
			if( linkItem.booleanValue() == true ){
				Path linkItemValue = makeCommonHelpers.getPath( requestJSON , JSON_LINK_ITEM_VALUE_KEY , message , status , true );
				Map<QName,Serializable> linkAspectProperties = new HashMap<QName,Serializable>();
				linkAspectProperties.put( WEBSITE_PAGE_LINK_ASPECT_LINK_TARGET_PROPERTY , linkItemValue );
				nodeService.addAspect( entry , WEBSITE_PAGE_LINK_ASPECT , linkAspectProperties );
				logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Added link aspect to \"" + name + "\" web page." );
			}
		}

		// news item
		if( newsPage != null ){
			logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] News page has \"" + newsPage.booleanValue() + "\" value." );
			if( nodeService.hasAspect( entry , WEBSITE_PAGE_NEWS_ASPECT ) ) nodeService.removeAspect( entry , WEBSITE_PAGE_NEWS_ASPECT );
			logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Removed news aspect from \"" + name + "\" web page." );
			if( newsPage.booleanValue() == true ){
				long newsPageDateLong = makeCommonHelpers.getNativeLong( requestJSON , JSON_NEWS_PAGE_DATE_KEY , message , status , false );
				if( newsPageDateLong > -1 ){
					Date newsPageDate = new Date( newsPageDateLong );
					Map<QName,Serializable> newsAspectProperties = new HashMap<QName,Serializable>();
					newsAspectProperties.put( WEBSITE_PAGE_NEWS_ASPECT_DATE_PROPERTY , newsPageDate );
					nodeService.addAspect( entry , WEBSITE_PAGE_NEWS_ASPECT , newsAspectProperties );
					logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Added news aspect to \"" + name + "\" web page." );
				}
			}
		}

		// event item
		if( eventPage != null ){
			logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Event page has \"" + eventPage.booleanValue() + "\" value." );

			String calendarEntryName = (String) nodeService.getProperty( entry , WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY );
			if( calendarEntryName != null && eventPage.booleanValue() == false ) {
				SiteInfo website = makeWebsiteCommon.getWebsite( message );
				makeCommonHelpers.removeCalendarEntry( website , calendarEntryName );
			}

			if( nodeService.hasAspect( entry , WEBSITE_PAGE_EVENT_ASPECT ) ) nodeService.removeAspect( entry , WEBSITE_PAGE_EVENT_ASPECT );
			logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Removed event aspect from \"" + name + "\" web page." );
			if( eventPage.booleanValue() == true ){
				long eventPageStartDateLong = makeCommonHelpers.getNativeLong( requestJSON , JSON_EVENT_PAGE_START_DATE_KEY , message , status , false );
				long eventPageEndDateLong = makeCommonHelpers.getNativeLong( requestJSON , JSON_EVENT_PAGE_END_DATE_KEY , message , status , false );
				if( eventPageStartDateLong > -1 ){
					Date eventPageStartDate = new Date( eventPageStartDateLong );
					Date eventPageEndDate = ( eventPageEndDateLong > -1 ) ? new Date( eventPageEndDateLong ) : eventPageStartDate;
					String eventPageLocation = makeCommonHelpers.getString( requestJSON , JSON_EVENT_PAGE_LOCATION_KEY , message , status , false );
					Boolean eventPageShowRegistrationForm = makeCommonHelpers.getBooleanObj( requestJSON , JSON_EVENT_PAGE_SHOW_REGISTRATION_FORM_KEY , message , status , false );
					String eventPageRegistrationFormTemplate = makeCommonHelpers.getString( requestJSON , JSON_EVENT_PAGE_REGISTRATION_FORM_TEMPLATE_KEY , message , status , false );
					String registrationCode = makeCommonHelpers.getString( requestJSON , JSON_REGISTRATION_CODE_KEY , message , status , false );

					Map<String,Serializable> eventCalendarProperties = new HashMap<String,Serializable>();
					eventCalendarProperties.put( CALENDAR_TITLE_PROPERTY_NAME , ( displayTitle != null ) ? displayTitle : title );
					eventCalendarProperties.put( CALENDAR_DESCRIPTION_PROPERTY_NAME , ( description != null ) ? description.replaceAll( "<[^>]*>?" , "" ) : null );
					eventCalendarProperties.put( CALENDAR_LOCATION_PROPERTY_NAME , ( eventPageLocation != null ) ? eventPageLocation.replaceAll( "<[^>]*>?" , "" ) : null );
					eventCalendarProperties.put( CALENDAR_START_DATE_PROPERTY_NAME , eventPageStartDate );
					eventCalendarProperties.put( CALENDAR_END_DATE_PROPERTY_NAME , eventPageEndDate );
					SiteInfo website = makeWebsiteCommon.getWebsite( message );

					CalendarEntry calendarEntry = null;
					if( calendarEntryName != null ){
						try{
							calendarEntry = makeCommonHelpers.getCalendarEntry( website , calendarEntryName );
						}
						catch( Exception e ){
							logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] No existing calendar entry \"" + calendarEntryName + "\" found. (" + e.getMessage() + ")" );
							calendarEntry = null;
						}
					}
					if( calendarEntry == null ){
						calendarEntry = makeCommonHelpers.addCalendarEntry( website , eventCalendarProperties );
						logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Added new calendar entry \"" + calendarEntryName + "\"." );
					}
					else{
						calendarEntry = makeCommonHelpers.updateCalendarEntry( calendarEntry , eventCalendarProperties );
						logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Updated calendar entry \"" + calendarEntryName + "\"." );
					}

					Map<QName,Serializable> eventAspectProperties = new HashMap<QName,Serializable>();
					eventAspectProperties.put( WEBSITE_PAGE_EVENT_ASPECT_DATE_PROPERTY , eventPageStartDate );
					eventAspectProperties.put( WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY , calendarEntry.getSystemName() );
					eventAspectProperties.put( WEBSITE_PAGE_EVENT_ASPECT_SHOW_REGISTRATION_FORM_PROPERTY , ( eventPageShowRegistrationForm != null ) ? eventPageShowRegistrationForm.booleanValue() : false );
					eventAspectProperties.put( WEBSITE_PAGE_EVENT_ASPECT_REGISTRATION_FORM_TEMPLATE_PROPERTY , ( eventPageRegistrationFormTemplate != null ) ? eventPageRegistrationFormTemplate : null );
					eventAspectProperties.put( WEBSITE_PAGE_EVENT_ASPECT_REGISTRATION_CODE_PROPERTY , ( registrationCode != null ) ? registrationCode : null );
					nodeService.addAspect( entry , WEBSITE_PAGE_EVENT_ASPECT , eventAspectProperties );
					logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Added event aspect to \"" + name + "\" web page." );
				}
			}
		}

		return entry;
	}

	private ArrayList<NodeRef> createStaticContents( NodeRef entry , JSONObject requestJSON , MakeMessage message , Status status ) throws Exception{
		ArrayList<NodeRef> staticEntries = new ArrayList<NodeRef>();
		@SuppressWarnings("unchecked")
		ArrayList<Object> containers = (ArrayList<Object>) nodeService.getProperty( entry , WEBSITE_PAGE_CONTAINERS_PROPERTY );
		if( containers == null ) return staticEntries;
		
		for( Object container : containers ){
			String method = (String) ( (JSONObject) container ).get( CONTAINER_METHOD_KEY );
			if( method != null && method.equals( CONTAINER_SHOW_CONTENT_VAL ) ){
				logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] Static content method \"" + method + "\" found in containers." );
				String containerName = (String) ( (JSONObject) container ).get( CONTAINER_NAME_KEY );
				if( containerName == null ) continue;
				logger.debug( "[" + MakeWebsitePageAdd.class.getName() + "] ... in container \"" + containerName + "\"." );
				String content = makeCommonHelpers.getString( requestJSON , containerName , message , status , false );
				if( content != null ){
					InputStream inputStream = new ByteArrayInputStream( content.getBytes( StandardCharsets.UTF_8 ) );
					NodeRef staticEntry = alfContent.createContentNode( entry , containerName , inputStream , StandardCharsets.UTF_8.name() , MimetypeMap.MIMETYPE_HTML , WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_CONTENT_TYPE_NAME , false );
					if( inputStream != null ) inputStream.close();
					staticEntries.add( staticEntry );
				}
			}
		}
		
		return staticEntries;
	}
}
