/*
 * Copyright (c) 2018 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.service.push;

import java.util.function.Consumer;

import org.eclipse.rap.rwt.service.ServerPushSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/** Puts RAP's server-push functionality in a well defined service by using a context
 * as state holder and the possibility to split the server-push process into two
 * separated processes, a business-process to get and update business data and the
 * an update-process to update the UI after according to updated data */
@Lazy
@Service
public class ServerPushService {

    private static final Logger log = LoggerFactory.getLogger(ServerPushService.class);

    public void runServerPush(
            final ServerPushContext context,
            final long intervalPause,
            final Consumer<ServerPushContext> update) {

        this.runServerPush(context, intervalPause, null, update);
    }

    public void runServerPush(
            final ServerPushContext context,
            final long intervalPause,
            final Consumer<ServerPushContext> business,
            final Consumer<ServerPushContext> update) {

        final ServerPushSession pushSession = new ServerPushSession();

        pushSession.start();
        final Thread bgThread = new Thread(() -> {
            while (!context.isDisposed() && context.runAgain()) {

                try {
                    Thread.sleep(intervalPause);
                } catch (final Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug("unexpected error while sleep: ", e);
                    }
                }

                if (business != null) {
                    try {
                        log.trace("Call business on Server Push Session on: {}", Thread.currentThread().getName());
                        business.accept(context);
                    } catch (final Exception e) {
                        log.error("Unexpected error while do business for server push service", e);
                        if (context.runAgain()) {
                            continue;
                        } else {
                            return;
                        }
                    }
                }

                if (!context.isDisposed()) {

                    log.trace("Call update on Server Push Session on: {}", Thread.currentThread().getName());

                    context.getDisplay().asyncExec(() -> {
                        try {
                            update.accept(context);
                        } catch (final Exception e) {
                            log.warn(
                                    "Failed to update on Server Push Session {}. It seems that the UISession is not available anymore. "
                                            + "This may source from a connection interruption. cause: {}",
                                    Thread.currentThread().getName(), e.getMessage());
                        }
                    });
                }
            }

            log.info("Stop Server Push Session on: {}", Thread.currentThread().getName());
            try {
                pushSession.stop();
            } catch (final Exception e) {
                log.warn(
                        "Failed to stop Server Push Session on: {}. It seems that the UISession is not available anymore. This may source from a connection interruption",
                        Thread.currentThread().getName(), e);
            }

        });

        log.info("Start new Server Push Session on: {}", bgThread.getName());

        bgThread.setDaemon(true);
        bgThread.start();
    }
}
