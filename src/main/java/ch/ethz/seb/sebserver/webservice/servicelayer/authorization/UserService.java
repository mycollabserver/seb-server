/*
 * Copyright (c) 2018 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.authorization;

import java.security.Principal;

import org.springframework.web.bind.WebDataBinder;

/** A service to get the authenticated user from current request */
public interface UserService {

    String USERS_INSTITUTION_AS_DEFAULT = "USERS_INSTITUTION_AS_DEFAULT";

    /** Use this to get the current User within a request-response thread cycle.
     *
     * @return the SEBServerUser instance of the current request
     * @throws IllegalStateException if no Authentication was found
     * @throws IllegalArgumentException if fromPrincipal is not able to extract the User of the Authentication
     *             instance */
    SEBServerUser getCurrentUser();

    /** Extracts the internal SEBServerUser from a given Principal.
     *
     * This is attended to apply some known strategies to extract the internal user from Principal. If there is no
     * internal user found on the given Principal, a IllegalArgumentException is thrown.
     *
     * If there is certainly a internal user within the given Principal but no strategy that finds it, this method can
     * be extended with the needed strategy.
     *
     * @param principal
     * @return internal User instance if it was found within the Principal and the existing strategies
     * @throws IllegalArgumentException if no internal User can be found */
    SEBServerUser extractFromPrincipal(final Principal principal);

    /** Gets an overall anonymous user with no rights. This can be used to make user specific data anonymous
     * so that they have only a reference to this anonymous user
     *
     * @return an overall anonymous user with no rights */
    SEBServerUser getAnonymousUser();

    /** Gets a overall super user with all rights. This can be used for background jobs that are
     * not user specific but system specific.
     *
     * @return an overall super user with all rights */
    SEBServerUser getSuperUser();

    /** Binds the current users institution identifier as default value to a
     * 
     * @RequestParam of type API.PARAM_INSTITUTION_ID if needed. See EntityController class for example
     * @param binder Springs WebDataBinder is injected on controller side */
    void addUsersInstitutionDefaultPropertySupport(final WebDataBinder binder);

}
