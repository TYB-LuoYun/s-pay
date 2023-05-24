package com.dicomclub.payment.module.pay.service.wxpay.v3.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author ftm
 * @date 2023/3/21 0021 10:54
 */

public class OriginNotifyResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("id")
    private String id;
    @SerializedName("create_time")
    private String createTime;
    @SerializedName("event_type")
    private String eventType;
    @SerializedName("summary")
    private String summary;
    @SerializedName("resource_type")
    private String resourceType;
    @SerializedName("resource")
    private OriginNotifyResponse.Resource resource;

    public String getId() {
        return this.id;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public String getEventType() {
        return this.eventType;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getResourceType() {
        return this.resourceType;
    }

    public OriginNotifyResponse.Resource getResource() {
        return this.resource;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setResource(OriginNotifyResponse.Resource resource) {
        this.resource = resource;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof OriginNotifyResponse)) {
            return false;
        } else {
            OriginNotifyResponse other = (OriginNotifyResponse)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$id = this.getId();
                Object other$id = other.getId();
                if (this$id == null) {
                    if (other$id != null) {
                        return false;
                    }
                } else if (!this$id.equals(other$id)) {
                    return false;
                }

                Object this$createTime = this.getCreateTime();
                Object other$createTime = other.getCreateTime();
                if (this$createTime == null) {
                    if (other$createTime != null) {
                        return false;
                    }
                } else if (!this$createTime.equals(other$createTime)) {
                    return false;
                }

                Object this$eventType = this.getEventType();
                Object other$eventType = other.getEventType();
                if (this$eventType == null) {
                    if (other$eventType != null) {
                        return false;
                    }
                } else if (!this$eventType.equals(other$eventType)) {
                    return false;
                }

                label62: {
                    Object this$summary = this.getSummary();
                    Object other$summary = other.getSummary();
                    if (this$summary == null) {
                        if (other$summary == null) {
                            break label62;
                        }
                    } else if (this$summary.equals(other$summary)) {
                        break label62;
                    }

                    return false;
                }

                label55: {
                    Object this$resourceType = this.getResourceType();
                    Object other$resourceType = other.getResourceType();
                    if (this$resourceType == null) {
                        if (other$resourceType == null) {
                            break label55;
                        }
                    } else if (this$resourceType.equals(other$resourceType)) {
                        break label55;
                    }

                    return false;
                }

                Object this$resource = this.getResource();
                Object other$resource = other.getResource();
                if (this$resource == null) {
                    if (other$resource != null) {
                        return false;
                    }
                } else if (!this$resource.equals(other$resource)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof OriginNotifyResponse;
    }

    public int hashCode() {
        int PRIME = 1;
        int result = 1;
        Object $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        Object $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : $createTime.hashCode());
        Object $eventType = this.getEventType();
        result = result * 59 + ($eventType == null ? 43 : $eventType.hashCode());
        Object $summary = this.getSummary();
        result = result * 59 + ($summary == null ? 43 : $summary.hashCode());
        Object $resourceType = this.getResourceType();
        result = result * 59 + ($resourceType == null ? 43 : $resourceType.hashCode());
        Object $resource = this.getResource();
        result = result * 59 + ($resource == null ? 43 : $resource.hashCode());
        return result;
    }

    public String toString() {
        return "OriginNotifyResponse(id=" + this.getId() + ", createTime=" + this.getCreateTime() + ", eventType=" + this.getEventType() + ", summary=" + this.getSummary() + ", resourceType=" + this.getResourceType() + ", resource=" + this.getResource() + ")";
    }

    public OriginNotifyResponse() {
    }

    public static class Resource implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("algorithm")
        private String algorithm;
        @SerializedName("original_type")
        private String originalType;
        @SerializedName("ciphertext")
        private String ciphertext;
        @SerializedName("associated_data")
        private String associatedData;
        @SerializedName("nonce")
        private String nonce;

        public String getAlgorithm() {
            return this.algorithm;
        }

        public String getOriginalType() {
            return this.originalType;
        }

        public String getCiphertext() {
            return this.ciphertext;
        }

        public String getAssociatedData() {
            return this.associatedData;
        }

        public String getNonce() {
            return this.nonce;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public void setOriginalType(String originalType) {
            this.originalType = originalType;
        }

        public void setCiphertext(String ciphertext) {
            this.ciphertext = ciphertext;
        }

        public void setAssociatedData(String associatedData) {
            this.associatedData = associatedData;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof OriginNotifyResponse.Resource)) {
                return false;
            } else {
                OriginNotifyResponse.Resource other = (OriginNotifyResponse.Resource)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    label71: {
                        Object this$algorithm = this.getAlgorithm();
                        Object other$algorithm = other.getAlgorithm();
                        if (this$algorithm == null) {
                            if (other$algorithm == null) {
                                break label71;
                            }
                        } else if (this$algorithm.equals(other$algorithm)) {
                            break label71;
                        }

                        return false;
                    }

                    Object this$originalType = this.getOriginalType();
                    Object other$originalType = other.getOriginalType();
                    if (this$originalType == null) {
                        if (other$originalType != null) {
                            return false;
                        }
                    } else if (!this$originalType.equals(other$originalType)) {
                        return false;
                    }

                    label57: {
                        Object this$ciphertext = this.getCiphertext();
                        Object other$ciphertext = other.getCiphertext();
                        if (this$ciphertext == null) {
                            if (other$ciphertext == null) {
                                break label57;
                            }
                        } else if (this$ciphertext.equals(other$ciphertext)) {
                            break label57;
                        }

                        return false;
                    }

                    Object this$associatedData = this.getAssociatedData();
                    Object other$associatedData = other.getAssociatedData();
                    if (this$associatedData == null) {
                        if (other$associatedData != null) {
                            return false;
                        }
                    } else if (!this$associatedData.equals(other$associatedData)) {
                        return false;
                    }

                    Object this$nonce = this.getNonce();
                    Object other$nonce = other.getNonce();
                    if (this$nonce == null) {
                        if (other$nonce == null) {
                            return true;
                        }
                    } else if (this$nonce.equals(other$nonce)) {
                        return true;
                    }

                    return false;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof OriginNotifyResponse.Resource;
        }

        public int hashCode() {
            int PRIME = 1;
            int result = 1;
            Object $algorithm = this.getAlgorithm();
             result = result * 59 + ($algorithm == null ? 43 : $algorithm.hashCode());
            Object $originalType = this.getOriginalType();
            result = result * 59 + ($originalType == null ? 43 : $originalType.hashCode());
            Object $ciphertext = this.getCiphertext();
            result = result * 59 + ($ciphertext == null ? 43 : $ciphertext.hashCode());
            Object $associatedData = this.getAssociatedData();
            result = result * 59 + ($associatedData == null ? 43 : $associatedData.hashCode());
            Object $nonce = this.getNonce();
            result = result * 59 + ($nonce == null ? 43 : $nonce.hashCode());
            return result;
        }

        public String toString() {
            return "OriginNotifyResponse.Resource(algorithm=" + this.getAlgorithm() + ", originalType=" + this.getOriginalType() + ", ciphertext=" + this.getCiphertext() + ", associatedData=" + this.getAssociatedData() + ", nonce=" + this.getNonce() + ")";
        }

        public Resource() {
        }
    }
}

