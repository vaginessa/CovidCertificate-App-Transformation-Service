/*
 * Copyright (c) 2021 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.covidcertificate.backend.transformation.ws.config;

import ch.admin.bag.covidcertificate.backend.transformation.ws.client.VerificationCheckClient;
import ch.admin.bag.covidcertificate.backend.transformation.ws.controller.TransformationController;
import ch.admin.bag.covidcertificate.backend.transformation.ws.util.MockHelper;
import ch.admin.bag.covidcertificate.backend.transformation.ws.util.OauthWebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public abstract class WsBaseConfig {

    @Value("${mock.url:test}")
    private String mockUrl;

    @Value("${ws.jwt.client-id:default-client}")
    private String clientId;

    @Value("${verification.check.baseurl}")
    private String verificationCheckBaseUrl;

    @Value("${verification.check.endpoint}")
    private String verificationCheckEndpoint;

    @Bean
    public TransformationController transformationController(
            MockHelper mockHelper,
            VerificationCheckClient verificationCheckClient,
            OauthWebClient tokenReceiver) {
        return new TransformationController(mockHelper, verificationCheckClient, tokenReceiver);
    }

    @Bean
    public OauthWebClient tokenReceiver(ClientRegistrationRepository clientRegistration) {
        return new OauthWebClient(clientId, clientRegistration);
    }

    @Bean
    public MockHelper mockHelper() {
        return new MockHelper(mockUrl);
    }

    @Bean
    public VerificationCheckClient verificationCheckClient() {
        return new VerificationCheckClient(verificationCheckBaseUrl, verificationCheckEndpoint);
    }
}
