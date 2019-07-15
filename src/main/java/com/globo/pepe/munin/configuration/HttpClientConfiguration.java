/*
 * Copyright (c) 2019. Globo.com - ATeam
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globo.pepe.munin.configuration;

import com.globo.pepe.common.services.JsonLoggerService;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.net.ssl.SSLException;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.Response;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {

    private final JsonLoggerService loggerService;

    public HttpClientConfiguration(JsonLoggerService loggerService) {
        this.loggerService = loggerService;
    }

    @Bean
    public HttpClient asyncHttpClient() {
        return new HttpClient();
    }

    public class HttpClient {

        private AsyncHttpClient asyncHttpClient = null;

        public HttpClient() {
            try {
                this.asyncHttpClient = Dsl.asyncHttpClient(Dsl.config()
                    .setConnectionTtl(10000)
                    .setPooledConnectionIdleTimeout(5000)
                    .setMaxConnections(10)
                    .setSslContext(
                        SslContextBuilder.forClient().sslProvider(SslProvider.JDK).trustManager(InsecureTrustManagerFactory.INSTANCE).build())
                    .build());
            } catch (SSLException e) {
                loggerService.newLogger(getClass()).message(e.getMessage()).sendError(e);
            }
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        public void getAndSave(String url, String destPath) throws IOException {
            final File file = new File(destPath);
            file.createNewFile();
            try (final FileOutputStream stream = new FileOutputStream(destPath)) {
                asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler<FileOutputStream>() {

                    @Override
                    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                        stream.getChannel().write(bodyPart.getBodyByteBuffer());
                        return State.CONTINUE;
                    }

                    @Override
                    public FileOutputStream onCompleted(Response response) throws Exception {
                        return stream;
                    }
                });
            } catch (RuntimeException e) {
                loggerService.newLogger(getClass()).message(String.valueOf(e.getCause())).sendError();
            }
        }
    }

}
