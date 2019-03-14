/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.core.env.Environment;

public class ClientCredentialServiceTest {

//    @Test
//    public void testEncryptSimpleSecret() {
//        final Environment envMock = mock(Environment.class);
//        when(envMock.getRequiredProperty(ClientCredentialService.SEBSERVER_WEBSERVICE_INTERNAL_SECRET_KEY))
//                .thenReturn("secret1");
//
//        final ClientCredentialService service = new ClientCredentialService(envMock);
//        final String encrypt = service.encrypt("text1");
//        final String decrypt = service.decrypt(encrypt);
//        assertEquals("text1", decrypt);
//    }

    @Test
    public void testEncryptDecryptClientCredentials() {
        final Environment envMock = mock(Environment.class);
        when(envMock.getRequiredProperty(ClientCredentialServiceImpl.SEBSERVER_WEBSERVICE_INTERNAL_SECRET_KEY))
                .thenReturn("secret1");

        final String clientName = "simpleClientName";

        final ClientCredentialServiceImpl service = new ClientCredentialServiceImpl(envMock);
        String encrypted =
                service.encrypt(clientName, "secret1", ClientCredentialServiceImpl.DEFAULT_SALT).toString();
        String decrypted = service.decrypt(encrypted, "secret1", ClientCredentialServiceImpl.DEFAULT_SALT).toString();

        assertEquals(clientName, decrypted);

        final String clientSecret = "fbjreij39ru29305ruà££àèLöäöäü65%(/%(ç87";

        encrypted =
                service.encrypt(clientSecret, "secret1", ClientCredentialServiceImpl.DEFAULT_SALT).toString();
        decrypted = service.decrypt(encrypted, "secret1", ClientCredentialServiceImpl.DEFAULT_SALT).toString();

        assertEquals(clientSecret, decrypted);
    }

}
