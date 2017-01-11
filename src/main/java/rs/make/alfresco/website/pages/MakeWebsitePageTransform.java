package rs.make.alfresco.website.pages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.log4j.Logger;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import rs.make.alfresco.alfcontent.AlfContent;
import rs.make.alfresco.common.message.MakeMessage;
import rs.make.alfresco.common.status.MakeStatus;
import rs.make.alfresco.common.webscripts.MakeCommonHelpers;
import rs.make.alfresco.common.webscripts.MakeCommonHelpers.OutputStatusProperties;
import rs.make.alfresco.request.Request;
import rs.make.alfresco.request.Request.Response;

public class MakeWebsitePageTransform extends AbstractWebScript {

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

	protected FileFolderService fileFolderService;
	public FileFolderService getFileFolderService() {
		return fileFolderService;
	}
	public void setFileFolderService( FileFolderService fileFolderService ) {
		this.fileFolderService = fileFolderService;
	}

	protected ContentService contentService;
	public ContentService getContentService() {
		return contentService;
	}
	public void setContentService( ContentService contentService ) {
		this.contentService = contentService;
	}

	protected MimetypeService mimetypeService;
	public MimetypeService getMimetypeService() {
		return mimetypeService;
	}
	public void setMimetypeService( MimetypeService mimetypeService ) {
		this.mimetypeService = mimetypeService;
	}

	protected Request request;
	public Request getRequest() {
		return request;
	}
	public void setRequest( Request request ) {
		this.request = request;
	}

	protected AlfContent alfContent;
	public AlfContent getAlfContent() {
		return alfContent;
	}
	public void setAlfContent( AlfContent alfContent ) {
		this.alfContent = alfContent;
	}

	private final String REQUEST_NODEREF_KEY = "nodeRef";
	private final String REQUEST_WEBSERVER_URI_KEY = "webserver-uri";

	private int responseThrowStatus = Status.STATUS_INTERNAL_SERVER_ERROR;

	private static Logger logger = Logger.getLogger( MakeWebsitePageTransform.class );

	@Override
	public void execute( WebScriptRequest req , WebScriptResponse res ) throws WebScriptException, IOException {
		try{
			WebScript webscript = req.getServiceMatch().getWebScript();
			MakeMessage message = new MakeMessage( webscript );

			String authenticatedUserName = AuthenticationUtil.getFullyAuthenticatedUser();
			if( authenticatedUserName == null ){
				authenticatedUserName = AuthenticationUtil.getGuestUserName();
				AuthenticationUtil.setRunAsUser( authenticatedUserName );
			}

			NodeRef nodeRef = resolveRequest( req , res , message , authenticatedUserName );
			if( nodeRef != null ) makeCommonHelpers.stream( res , nodeRef , null , null );
		}
		catch( Exception e ) {
			try{
				logger.debug( "[" + MakeWebsitePageTransform.class.getName() + "] Error message" + e.getMessage() );
				logger.debug( "[" + MakeWebsitePageTransform.class.getName() + "] Error cause" + ( ( e.getCause() != null ) ? e.getCause().getMessage() : "" ) );
				responseThrowStatus = ( e.getCause() != null ) ? Integer.parseInt( e.getCause().getMessage() , 10 ) : Status.STATUS_INTERNAL_SERVER_ERROR;
			}
			catch( Exception rtse ){
				logger.error( "[" + MakeWebsitePageTransform.class.getName() + "] " + rtse.getMessage() );
			}
			logger.error( "[" + MakeWebsitePageTransform.class.getName() + "] " + e.getMessage() );
			OutputStatusProperties outputResponse = makeCommonHelpers.outputStatus( req , res , e , responseThrowStatus );
			sendStatus( req , res , outputResponse.getStatus() , outputResponse.getCache() , outputResponse.getFormat() , outputResponse.getModel() );
		}
	}
	
	private NodeRef resolveRequest( WebScriptRequest req , WebScriptResponse res , MakeMessage message , String authenticatedUserName ) throws Exception {
		NodeRef transformedNodeRef = null;
		NodeRef nodeRef = makeCommonHelpers.getNode( req , REQUEST_NODEREF_KEY , message , null , true );
		String webserverURI = makeCommonHelpers.getString( req , REQUEST_WEBSERVER_URI_KEY , message , null , true );

		NodeRef websiteRootNode = makeWebsiteCommon.getWebsiteRootNode( message );
		List<String> arrPath = fileFolderService.getNameOnlyPath( websiteRootNode, nodeRef );
		String link = URLDecoder.decode( webserverURI , StandardCharsets.UTF_8.name() ) + new String( "/" ).concat( String.join( "/" , arrPath ) );

		Response response = request.get( link );
		int responseCode = response.getCode();
		if( responseCode != 200 ){
			String errorMessage = "Error occured whist trying to retrieve content from \"" + link + "\". " + response.getStatusLine().getReasonPhrase();
			throw new Exception( errorMessage , new Throwable( String.valueOf( Status.STATUS_SEE_OTHER ) ) );
		}
		String content = response.getContent();

		AuthenticationUtil.setRunAsUserSystem();

		MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
		MimeType transformedSourceExtensionMimeType = allTypes.forName( MimetypeMap.MIMETYPE_HTML );
		String transformedSourceExtension = transformedSourceExtensionMimeType.getExtension();
		String sourceName = (String) nodeService.getProperty( nodeRef, ContentModel.PROP_NAME ) + "." + transformedSourceExtension;
		
		String nodeName = (String) nodeService.getProperty( nodeRef, ContentModel.PROP_NAME );
		InputStream sourceStream = new ByteArrayInputStream( content.getBytes( StandardCharsets.UTF_8 ) );
		NodeRef transformedSourceNodeRef = alfContent.createContentNode( nodeRef , sourceName , sourceStream , StandardCharsets.UTF_8.name() , MimetypeMap.MIMETYPE_HTML , NamespaceService.CONTENT_MODEL_1_0_URI , ContentModel.TYPE_CONTENT.getLocalName() , false );
		sourceStream.close();
		if( transformedSourceNodeRef == null ){
			String errorMessage = "Could not create tranformation source \"" + sourceName + "\" in \"" + nodeName + "\".";
			throw new Exception( errorMessage , new Throwable( String.valueOf( Status.STATUS_INTERNAL_SERVER_ERROR ) ) );
		}

		ContentTransformer contentTransformer = contentService.getTransformer( MimetypeMap.MIMETYPE_HTML , MimetypeMap.MIMETYPE_PDF );
		if( contentTransformer == null ){
			String errorMessage = "No transformer found for conversion from \"" + MimetypeMap.MIMETYPE_HTML + "\" to \"" + MimetypeMap.MIMETYPE_PDF + "\".";
			throw new Exception( errorMessage , new Throwable( String.valueOf( Status.STATUS_EXPECTATION_FAILED ) ) );
		}

		MimeType transformedExtensionMimeType = allTypes.forName( MimetypeMap.MIMETYPE_PDF );
		String transformedExtension = transformedExtensionMimeType.getExtension();
		String name = (String) nodeService.getProperty( nodeRef, ContentModel.PROP_NAME ) + "." + transformedExtension;

		ContentReader contentReader = contentService.getReader( transformedSourceNodeRef , ContentModel.PROP_CONTENT );
		transformedNodeRef = alfContent.createContentNode( nodeRef , name , contentReader.getContentInputStream() , StandardCharsets.UTF_8.name() , MimetypeMap.MIMETYPE_PDF , NamespaceService.CONTENT_MODEL_1_0_URI , ContentModel.TYPE_CONTENT.getLocalName() , false );
		if( transformedNodeRef == null ){
			String errorMessage = "Could not create \"" + name + "\" in \"" + nodeName + "\".";
			throw new Exception( errorMessage , new Throwable( String.valueOf( Status.STATUS_EXPECTATION_FAILED ) ) );
		}

		ContentWriter writter = contentService.getWriter( transformedNodeRef , ContentModel.PROP_CONTENT , true );
		contentTransformer.transform( contentReader , writter );
		AuthenticationUtil.setRunAsUser( authenticatedUserName );

		return transformedNodeRef;
	}
}
