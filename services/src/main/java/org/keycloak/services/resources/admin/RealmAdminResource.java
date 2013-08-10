package org.keycloak.services.resources.admin;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.logging.Logger;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.models.RealmModel;
import org.keycloak.services.models.RoleModel;
import org.keycloak.services.models.UserModel;
import org.keycloak.services.resources.Transaction;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RealmAdminResource {
    protected static final Logger logger = Logger.getLogger(RealmAdminResource.class);
    protected UserModel admin;
    protected RealmModel realm;

    public RealmAdminResource(UserModel admin, RealmModel realm) {
        this.admin = admin;
        this.realm = realm;
    }

    @Path("applications")
    public ApplicationsResource getResources() {
        return new ApplicationsResource(admin, realm);
    }

    @GET
    @NoCache
    @Produces("application/json")
    public RealmRepresentation getRealm() {
        return new Transaction() {
            @Override
            protected RealmRepresentation callImpl() {
                return new RealmManager(session).toRepresentation(realm);
            }
        }.call();
    }


    @Path("roles")
    @GET
    @NoCache
    @Produces("application/json")
    public List<RoleRepresentation> getRoles() {
        return new Transaction() {
            @Override
            protected List<RoleRepresentation> callImpl() {
                List<RoleModel> roleModels = realm.getRoles();
                List<RoleRepresentation> roles = new ArrayList<RoleRepresentation>();
                for (RoleModel roleModel : roleModels) {
                    RoleRepresentation role = new RoleRepresentation(roleModel.getName(), roleModel.getDescription());
                    roles.add(role);
                }
                return roles;
            }
        }.call();
    }

    @PUT
    @Consumes("application/json")
    public void updateRealm(final RealmRepresentation rep) {
        new Transaction() {
            @Override
            protected void runImpl() {
                logger.info("updating realm: " + rep.getRealm());
                new RealmManager(session).updateRealm(rep, realm);
            }
        }.run();

    }

    @Path("roles/{id}")
    @GET
    @NoCache
    @Produces("application/json")
    public RoleRepresentation getRole(final @PathParam("id") String id) {
        return new Transaction() {
            @Override
            protected RoleRepresentation callImpl() {
                RoleModel roleModel = realm.getRoleById(id);
                if (roleModel == null) {
                    throw new NotFoundException();
                }
                RoleRepresentation rep = new RoleRepresentation(roleModel.getName(), roleModel.getDescription());
                rep.setId(roleModel.getId());
                return rep;
            }
        }.call();
    }


    @Path("roles/{id}")
    @PUT
    @Consumes("application/json")
    public void updateRole(final @PathParam("id") String id, final RoleRepresentation rep) {
        new Transaction() {
            @Override
            protected void runImpl() {
                RoleModel role = realm.getRoleById(id);
                if (role == null) {
                   throw new NotFoundException();
                }
                role.setName(rep.getName());
                role.setDescription(rep.getDescription());
            }
        }.run();

    }

    @Path("roles")
    @POST
    @Consumes("application/json")
    public Response createRole(final @Context UriInfo uriInfo, final RoleRepresentation rep) {
        return new Transaction() {
            @Override
            protected Response callImpl() {
                if (realm.getRole(rep.getName()) != null) {
                   throw new InternalServerErrorException(); // todo appropriate status here.
                }
                RoleModel role = realm.addRole(rep.getName());
                if (role == null) {
                    throw new NotFoundException();
                }
                role.setDescription(rep.getDescription());
                return Response.created(uriInfo.getAbsolutePathBuilder().path(role.getId()).build()).build();
            }
        }.call();

    }


    @Path("users")
    @GET
    @NoCache
    @Produces("application/json")
    public List<UserRepresentation> getUsers() {
        return null;
    }




}