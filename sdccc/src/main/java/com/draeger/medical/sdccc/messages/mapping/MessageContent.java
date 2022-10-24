/*
 * This Source Code Form is subject to the terms of the MIT License.
 * Copyright (c) 2022 Draegerwerk AG & Co. KGaA.
 *
 * SPDX-License-Identifier: MIT
 */

package com.draeger.medical.sdccc.messages.mapping;

import com.draeger.medical.sdccc.messages.util.MessageUtil;
import org.hibernate.annotations.GenericGenerator;
import org.somda.sdc.dpws.CommunicationLog;
import org.somda.sdc.dpws.soap.CommunicationContext;
import org.somda.sdc.dpws.soap.HttpApplicationInfo;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * POJO for persisting relevant message information.
 */
@Entity(name = "MessageContent")
@Table(name = "message_content")
public class MessageContent {

    @Transient
    private static final int MAXIMUM_LENGTH = 2147483647;

    @Id
    @GenericGenerator(name = "MessageContentIDGen", strategy = "increment")
    @GeneratedValue(generator = "MessageContentIDGen")
    private long incId;

    @Column(columnDefinition = "clob", length = MAXIMUM_LENGTH)
    private String body;

    @ElementCollection
    @Column(columnDefinition = "blob", length = MAXIMUM_LENGTH)
    private List<X509Certificate> certs;

    @OneToMany(
        cascade = CascadeType.ALL,
        mappedBy = "messageContent",
        orphanRemoval = true
    )
    private List<StringEntryEntity> headers;

    @ElementCollection
    private Set<String> actions;

    @ElementCollection
    private Set<String> bodyElements;

    private CommunicationLog.Direction direction;
    private CommunicationLog.MessageType messageType;

    @Column(nullable = true)
    private String transactionId;
    private String requestUri;
    private long timestamp;
    private long nanoTimestamp;
    private String messageHash;
    private String scheme;
    private String uuid;
    private boolean isSOAP;


    /**
     * This will be used by hibernate when creating the POJO from database entries.
     */
    public MessageContent() {
    }


    /**
     * This will be used when creating the POJO before loading it into the database.
     *
     * @param body                 data send on top of the transport or application layer
     * @param communicationContext information about the transport and application layer
     * @param direction            declares, if the message was outgoing or ingoing
     * @param messageType          type of the message, i.e. request, response
     * @param timestamp            time point of the stream creation for getting the body
     * @param nanoTimestamp        point in time relative to current jvm start at which message arrived,
     *                             useful for sorting
     * @param actions              ws addressing actions
     * @param bodyElements         QNames of all elements present in the soap body
     * @param uuid                 identifier for ensuring, that a message was written to the database
     * @param isSOAP               shall be true if a SOAP envelope was found and false otherwise
     */
    public MessageContent(final String body,
                          final CommunicationContext communicationContext,
                          final CommunicationLog.Direction direction,
                          final CommunicationLog.MessageType messageType,
                          final long timestamp,
                          final long nanoTimestamp,
                          final Set<String> actions,
                          final Set<String> bodyElements,
                          final String uuid,
                          final boolean isSOAP) {

        this.body = body;
        this.direction = direction;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.nanoTimestamp = nanoTimestamp;
        this.actions = actions;
        this.bodyElements = bodyElements;
        this.uuid = uuid;
        this.isSOAP = isSOAP;

        this.messageHash = MessageUtil.hashMessage(this.body);
        this.scheme = communicationContext.getTransportInfo().getScheme();

        this.certs = communicationContext.getTransportInfo().getX509Certificates();

        final List<StringEntryEntity> headersMap;
        // handle http headers
        if (communicationContext.getApplicationInfo() instanceof HttpApplicationInfo) {
            final HttpApplicationInfo httpAppInfo = (HttpApplicationInfo) communicationContext.getApplicationInfo();

            headersMap = new ArrayList<>();
            for (final Map.Entry<String, Collection<String>> entry : httpAppInfo.getHeaders().asMap().entrySet()) {
                final String key = entry.getKey();
                final Collection<String> value = entry.getValue();
                value.forEach(element -> headersMap.add(new StringEntryEntity(key, element, this)));
            }
            transactionId = httpAppInfo.getTransactionId();
            requestUri = httpAppInfo.getRequestUri().orElse(null);
        } else {
            headersMap = Collections.emptyList();
            transactionId = null;
            requestUri = null;
        }
        this.headers = headersMap;
    }

    public String getBody() {
        return this.body;
    }

    public String getScheme() {
        return this.scheme;
    }

    public Set<String> getActions() {
        return this.actions;
    }

    public String getUuid() {
        return this.uuid;
    }

    public boolean getIsSOAP() {
        return this.isSOAP;
    }

    public String getMessageHash() {
        return this.messageHash;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getNanoTimestamp() {
        return nanoTimestamp;
    }

    public List<X509Certificate> getCerts() {
        return this.certs;
    }

    public Map<String, List<String>> getHeaders() {
        final HashMap<String, List<String>> headersMap = new HashMap<>();

        for (final StringEntryEntity entry : this.headers) {
            if (!headersMap.containsKey(entry.getEntryKey())) {
                headersMap.put(entry.getEntryKey(), new ArrayList<>());
            }
            headersMap.get(entry.getEntryKey()).add(entry.getEntryValue());
        }

        return headersMap;
    }

    public CommunicationLog.Direction getDirection() {
        return this.direction;
    }

    public CommunicationLog.MessageType getMessageType() {
        return this.messageType;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public String getRequestUri() {
        return this.requestUri;
    }
    
    public Set<String> getBodyElements() {
        return bodyElements;
    }
}