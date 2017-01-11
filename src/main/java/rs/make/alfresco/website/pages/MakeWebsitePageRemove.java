package rs.make.alfresco.website.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;

import rs.make.alfresco.common.message.MakeMessage;
import rs.make.alfresco.common.status.MakeStatus;
import rs.make.alfresco.common.webscripts.MakeCommonHelpers;

public class MakeWebsitePageRemove extends DeclarativeWebScript {

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

	protected NodeService nodeService;
	public NodeService getNodeService() {
		return nodeService;
	}
	public void setNodeService( NodeService nodeService ) {
		this.nodeService = nodeService;
	}

	protected NamespaceService namespaceService;
	public NamespaceService getNamespaceService() {
		return namespaceService;
	}
	public void setNamespaceService( NamespaceService namespaceService ) {
		this.namespaceService = namespaceService;
	}

	protected DictionaryService dictionaryService;
	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}
	public void setDictionaryService( DictionaryService dictionaryService ) {
		this.dictionaryService = dictionaryService;
	}

	private final String WEBSITE_PAGE_NAMESPACE_URI = "http://www.make.rs/model/website/1.0";
	private final String WEBSITE_PAGE_TYPE_NAME = "webSitePage";
	private final QName WEBSITE_PAGE_TYPE = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_TYPE_NAME );
	private final String WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY_NAME = "calendarEntry";
	private final QName WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY = QName.createQName( WEBSITE_PAGE_NAMESPACE_URI , WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY_NAME );

	private final String WEBSITE_ADMIN_AUTHORITY = "website-admins";
	private final String REQUEST_NODEREF_KEY = "nodeRef";

	private boolean statusThrown = false;

	private static Logger logger = Logger.getLogger( MakeWebsitePageRemove.class );

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

			NodeRef nodeRef = makeCommonHelpers.getNode( req , REQUEST_NODEREF_KEY , message , status , true );
			QName nodeType = nodeService.getType( nodeRef );
			Collection<QName> websiteSubtypes = dictionaryService.getSubTypes( WEBSITE_PAGE_TYPE , true );
			boolean typeMatch = websiteSubtypes.stream().anyMatch( p -> p.equals( nodeType ) );

			if( !typeMatch ){
				ArrayList<String> args = new ArrayList<String>(2);
				args.add( nodeType.toPrefixString( namespaceService ) );
				args.add( WEBSITE_PAGE_TYPE.toPrefixString( namespaceService ) );
				String errorMessage = message.get( "error.notWebpageType" , args );
				throw new Exception( errorMessage , new Throwable( String.valueOf( Status.STATUS_FORBIDDEN ) ) );
			}

			String calendarEntryName = (String) nodeService.getProperty( nodeRef , WEBSITE_PAGE_EVENT_ASPECT_CALENDAR_ENTRY_PROPERTY );
			if( calendarEntryName != null ) {
				SiteInfo website = makeWebsiteCommon.getWebsite( message );
				makeCommonHelpers.removeCalendarEntry( website , calendarEntryName );
			}

			String nodeName = (String) nodeService.getProperty( nodeRef , ContentModel.PROP_NAME );
			nodeService.deleteNode( nodeRef );
			logger.debug( "[" + MakeWebsitePageRemove.class.getName() + "] \" Node " + nodeName + " deleted." );

			model.put( "response", true );
			String parsedMessage = message.get( "success.text" , null );
			model.put( "success", ( parsedMessage != null ) ? parsedMessage : "" );

			AuthenticationUtil.setRunAsUser( authenticatedUserName );
		}
		catch( Exception e ) {
			if( !statusThrown ) makeStatus.throwStatus( e.getMessage() , status , Status.STATUS_INTERNAL_SERVER_ERROR , MakeWebsitePageRemove.class );
			return null;
		}
		return model;
	}

}
