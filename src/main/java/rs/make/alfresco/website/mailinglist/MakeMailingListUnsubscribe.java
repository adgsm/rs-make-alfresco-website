package rs.make.alfresco.website.mailinglist;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.*;
import rs.make.alfresco.common.message.MakeMessage;
import rs.make.alfresco.common.status.MakeStatus;
import rs.make.alfresco.common.webscripts.MakeCommonHelpers;

import java.util.*;

public class MakeMailingListUnsubscribe extends DeclarativeWebScript {
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

    protected NamespaceService namespaceService;
    public NamespaceService getNamespaceService() {
        return namespaceService;
    }
    public void setNamespaceService( NamespaceService namespaceService ) {
        this.namespaceService = namespaceService;
    }

    protected SearchService searchService;
    public SearchService getSearchService() {
        return searchService;
    }
    public void setSearchService( SearchService searchService ) {
        this.searchService = searchService;
    }

    protected NodeService nodeService;
    public NodeService getNodeService() {
        return nodeService;
    }
    public void setNodeService( NodeService nodeService ) {
        this.nodeService = nodeService;
    }

    private final StoreRef STOREREF = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
    private final String REQUEST_EMAIL_KEY = "email";

    private final String MAILINGLIST_NAMESPACE_URI = "http://www.make.rs/model/mailinglist/1.0";
    private final String MAILINGLIST_TYPE_NAME = "mailingList";
    private final QName MAILINGLIST_TYPE = QName.createQName( MAILINGLIST_NAMESPACE_URI , MAILINGLIST_TYPE_NAME );
    private final String MAILINGLIST_EMAIL_PROPERTY_NAME = "email";
    private final QName MAILINGLIST_EMAIL_PROPERTY = QName.createQName( MAILINGLIST_NAMESPACE_URI , MAILINGLIST_EMAIL_PROPERTY_NAME );

    private boolean statusThrown = false;

    private static Logger logger = Logger.getLogger( MakeMailingListUnsubscribe.class );

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            WebScript webscript = req.getServiceMatch().getWebScript();
            MakeMessage message = new MakeMessage(webscript);

            AuthenticationUtil.setRunAsUserSystem();

            ResultSet mailingLists = executeQuery( req , status , message );
            String email = makeCommonHelpers.getString( req , REQUEST_EMAIL_KEY , message , status , false );
            for( NodeRef mailingList : mailingLists.getNodeRefs() ){
                logger.debug(String.format("[%s] Found requested email in \"%s\"", MakeMailingListUnsubscribe.class.getName(), nodeService.getProperty(mailingList, ContentModel.PROP_NAME).toString()));
                ArrayList<String> emails = (ArrayList<String>) nodeService.getProperty(mailingList, MAILINGLIST_EMAIL_PROPERTY);
                boolean removed  = emails.removeAll(Collections.singleton(email));
                if(removed == true)
                   nodeService.setProperty(mailingList, MAILINGLIST_EMAIL_PROPERTY, emails);
            }

            model.put("response", true);
            String parsedMessage = message.get("success.text", null);
            model.put("success", (parsedMessage != null) ? parsedMessage : "");

        } catch (Exception e) {
            if (!statusThrown)
                makeStatus.throwStatus(e.getMessage(), status, Status.STATUS_INTERNAL_SERVER_ERROR, MakeMailingListUnsubscribe.class);
            return null;
        }
        return model;
    }

    private ResultSet executeQuery( WebScriptRequest req , Status status , MakeMessage message ) throws Exception {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore( STOREREF );
        searchParameters.setLanguage( SearchService.LANGUAGE_LUCENE );

        String email = makeCommonHelpers.getString( req , REQUEST_EMAIL_KEY , message , status , false );
        if(email == null || email.equals(""))
            throw new Exception("No email provided");

        String query = "+TYPE:\"" + MAILINGLIST_TYPE.toPrefixString( namespaceService ) + "\"";
        ArrayList<QName> searchThrough = new ArrayList<QName>(1);
        searchThrough.add( MAILINGLIST_EMAIL_PROPERTY );
        query += makeCommonHelpers.getMetadataContainsQuery( email , searchThrough );

        searchParameters.setQuery( query );

        return searchService.query( searchParameters );
    }
}