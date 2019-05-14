package com.globo.pepe.munin.util;

import com.google.common.collect.SortedSetMultimap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.artifact.ArtifactService;
import org.openstack4j.api.barbican.BarbicanService;
import org.openstack4j.api.compute.ComputeService;
import org.openstack4j.api.dns.v2.DNSService;
import org.openstack4j.api.gbp.GbpService;
import org.openstack4j.api.heat.HeatService;
import org.openstack4j.api.identity.v3.IdentityService;
import org.openstack4j.api.image.ImageService;
import org.openstack4j.api.magnum.MagnumService;
import org.openstack4j.api.manila.ShareService;
import org.openstack4j.api.murano.v1.AppCatalogService;
import org.openstack4j.api.networking.NetworkingService;
import org.openstack4j.api.networking.ext.ServiceFunctionChainService;
import org.openstack4j.api.octavia.OctaviaService;
import org.openstack4j.api.sahara.SaharaService;
import org.openstack4j.api.senlin.SenlinService;
import org.openstack4j.api.storage.BlockStorageService;
import org.openstack4j.api.storage.ObjectStorageService;
import org.openstack4j.api.tacker.TackerService;
import org.openstack4j.api.telemetry.TelemetryService;
import org.openstack4j.api.trove.TroveService;
import org.openstack4j.api.types.Facing;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.api.workflow.WorkflowService;
import org.openstack4j.model.identity.AuthStore;
import org.openstack4j.model.identity.AuthVersion;
import org.openstack4j.model.identity.v3.Domain;
import org.openstack4j.model.identity.v3.Project;
import org.openstack4j.model.identity.v3.Role;
import org.openstack4j.model.identity.v3.Service;
import org.openstack4j.model.identity.v3.Token;
import org.openstack4j.model.identity.v3.User;
import org.openstack4j.model.identity.v3.builder.ProjectBuilder;

public class KeystoneMock {

    public static OSClientV3 getOSClientV3() {
        return new OSClientV3() {
            @Override
            public Token getToken() {
                return new Token() {
                    @Override
                    public String getId() {
                        return "00";
                    }

                    @Override
                    public List<? extends Service> getCatalog() {
                        return null;
                    }

                    @Override
                    public Date getExpires() {
                        return null;
                    }

                    @Override
                    public Date getIssuedAt() {
                        return null;
                    }

                    @Override
                    public Project getProject() {
                        return new Project() {
                            @Override
                            public String getId() {
                                return "000";
                            }

                            @Override
                            public String getDomainId() {
                                return null;
                            }

                            @Override
                            public Domain getDomain() {
                                return null;
                            }

                            @Override
                            public String getDescription() {
                                return "Description";
                            }

                            @Override
                            public String getName() {
                                return "Project";
                            }

                            @Override
                            public Map<String, String> getLinks() {
                                return null;
                            }

                            @Override
                            public String getParentId() {
                                return null;
                            }

                            @Override
                            public String getSubtree() {
                                return null;
                            }

                            @Override
                            public String getParents() {
                                return null;
                            }

                            @Override
                            public boolean isEnabled() {
                                return false;
                            }

                            @Override
                            public String getExtra(String s) {
                                return null;
                            }

                            @Override
                            public List<String> getTags() {
                                return null;
                            }

                            @Override
                            public ProjectBuilder toBuilder() {
                                return null;
                            }
                        };
                    }

                    @Override
                    public Domain getDomain() {
                        return null;
                    }

                    @Override
                    public User getUser() {
                        return null;
                    }

                    @Override
                    public AuthStore getCredentials() {
                        return null;
                    }

                    @Override
                    public String getEndpoint() {
                        return null;
                    }

                    @Override
                    public List<? extends Role> getRoles() {
                        return null;
                    }

                    @Override
                    public List<String> getAuditIds() {
                        return null;
                    }

                    @Override
                    public List<String> getMethods() {
                        return null;
                    }

                    @Override
                    public AuthVersion getVersion() {
                        return null;
                    }

                    @Override
                    public String getCacheIdentifier() {
                        return null;
                    }

                    @Override
                    public void setId(String s) {

                    }

                    @Override
                    public SortedSetMultimap<String, Service> getAggregatedCatalog() {
                        return null;
                    }
                };
            }

            @Override
            public IdentityService identity() {
                return null;
            }

            @Override
            public OSClientV3 useRegion(String s) {
                return null;
            }

            @Override
            public OSClientV3 removeRegion() {
                return null;
            }

            @Override
            public OSClientV3 perspective(Facing facing) {
                return null;
            }

            @Override
            public OSClientV3 headers(Map<String, ?> map) {
                return null;
            }

            @Override
            public Set<ServiceType> getSupportedServices() {
                return null;
            }

            @Override
            public boolean supportsCompute() {
                return false;
            }

            @Override
            public boolean supportsIdentity() {
                return false;
            }

            @Override
            public boolean supportsNetwork() {
                return false;
            }

            @Override
            public boolean supportsImage() {
                return false;
            }

            @Override
            public boolean supportsHeat() {
                return false;
            }

            @Override
            public boolean supportsMurano() {
                return false;
            }

            @Override
            public boolean supportsBlockStorage() {
                return false;
            }

            @Override
            public boolean supportsObjectStorage() {
                return false;
            }

            @Override
            public boolean supportsTelemetry() {
                return false;
            }

            @Override
            public boolean supportsShare() {
                return false;
            }

            @Override
            public String getEndpoint() {
                return null;
            }

            @Override
            public ComputeService compute() {
                return null;
            }

            @Override
            public NetworkingService networking() {
                return null;
            }

            @Override
            public ServiceFunctionChainService sfc() {
                return null;
            }

            @Override
            public OctaviaService octavia() {
                return null;
            }

            @Override
            public ArtifactService artifact() {
                return null;
            }

            @Override
            public TackerService tacker() {
                return null;
            }

            @Override
            public BlockStorageService blockStorage() {
                return null;
            }

            @Override
            public ObjectStorageService objectStorage() {
                return null;
            }

            @Override
            public ImageService images() {
                return null;
            }

            @Override
            public org.openstack4j.api.image.v2.ImageService imagesV2() {
                return null;
            }

            @Override
            public TelemetryService telemetry() {
                return null;
            }

            @Override
            public ShareService share() {
                return null;
            }

            @Override
            public HeatService heat() {
                return null;
            }

            @Override
            public AppCatalogService murano() {
                return null;
            }

            @Override
            public SaharaService sahara() {
                return null;
            }

            @Override
            public WorkflowService workflow() {
                return null;
            }

            @Override
            public MagnumService magnum() {
                return null;
            }

            @Override
            public GbpService gbp() {
                return null;
            }

            @Override
            public SenlinService senlin() {
                return null;
            }

            @Override
            public TroveService trove() {
                return null;
            }

            @Override
            public BarbicanService barbican() {
                return null;
            }

            @Override
            public DNSService dns() {
                return null;
            }
        };

    }


    public static Token getToken() {
        return new Token() {
            @Override
            public String getId() {
                return "00";
            }

            @Override
            public List<? extends Service> getCatalog() {
                return null;
            }

            @Override
            public Date getExpires() {
                return null;
            }

            @Override
            public Date getIssuedAt() {
                return null;
            }

            @Override
            public Project getProject() {
                return new Project() {
                    @Override
                    public String getId() {
                        return "000";
                    }

                    @Override
                    public String getDomainId() {
                        return null;
                    }

                    @Override
                    public Domain getDomain() {
                        return null;
                    }

                    @Override
                    public String getDescription() {
                        return "Description";
                    }

                    @Override
                    public String getName() {
                        return "Project";
                    }

                    @Override
                    public Map<String, String> getLinks() {
                        return null;
                    }

                    @Override
                    public String getParentId() {
                        return null;
                    }

                    @Override
                    public String getSubtree() {
                        return null;
                    }

                    @Override
                    public String getParents() {
                        return null;
                    }

                    @Override
                    public boolean isEnabled() {
                        return false;
                    }

                    @Override
                    public String getExtra(String s) {
                        return null;
                    }

                    @Override
                    public List<String> getTags() {
                        return null;
                    }

                    @Override
                    public ProjectBuilder toBuilder() {
                        return null;
                    }
                };
            }

            @Override
            public Domain getDomain() {
                return null;
            }

            @Override
            public User getUser() {
                return null;
            }

            @Override
            public AuthStore getCredentials() {
                return null;
            }

            @Override
            public String getEndpoint() {
                return null;
            }

            @Override
            public List<? extends Role> getRoles() {
                return null;
            }

            @Override
            public List<String> getAuditIds() {
                return null;
            }

            @Override
            public List<String> getMethods() {
                return null;
            }

            @Override
            public AuthVersion getVersion() {
                return null;
            }

            @Override
            public String getCacheIdentifier() {
                return null;
            }

            @Override
            public void setId(String s) {

            }

            @Override
            public SortedSetMultimap<String, Service> getAggregatedCatalog() {
                return null;
            }
        };
    }
}
