/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.loom_20200607;

import fi.iki.elonen.NanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND;

public final class ExampleHTTPSystem
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ExampleHTTPSystem.class);

  private Server server;

  public ExampleHTTPSystem()
  {

  }

  public void start()
    throws Exception
  {
    LOG.info("start");

    this.server = new Server("::1", 9090);
    this.server.setAsyncRunner(new Runner());
    this.server.start();
  }

  public void stop()
  {
    LOG.info("stop");
    this.server.stop();
  }

  private static final class Runner extends NanoHTTPD.DefaultAsyncRunner
  {
    private long requestCount;
    private Map<NanoHTTPD.ClientHandler, Thread> running;

    Runner()
    {
      this.running = new ConcurrentHashMap<>();
    }

    @Override
    public List<NanoHTTPD.ClientHandler> getRunning()
    {
      return List.copyOf(this.running.keySet());
    }

    @Override
    public void closeAll()
    {
      for (final var clientHandler : this.getRunning()) {
        clientHandler.close();
      }
    }

    @Override
    public void closed(
      final NanoHTTPD.ClientHandler clientHandler)
    {
      this.running.remove(clientHandler);
    }

    @Override
    public void exec(final NanoHTTPD.ClientHandler clientHandler)
    {
      ++this.requestCount;

      final var name =
        String.format("exHttpRequest[%d]", Long.valueOf(this.requestCount));

      final Thread thread =
        Thread.builder()
          .virtual()
          .name(name)
          .task(clientHandler)
          .build();

      this.running.put(clientHandler, thread);
      thread.start();
    }
  }

  private static final class Server extends NanoHTTPD
  {
    Server(
      final String hostname,
      final int port)
    {
      super(hostname, port);
    }

    private static Response showDefault()
    {
      return newFixedLengthResponse(
        NOT_FOUND, "text/plain", "Not found."
      );
    }

    private static Response showTasks()
    {
      final var text = new StringBuilder(128);
      Thread.getAllStackTraces()
        .keySet()
        .stream()
        .sorted(Comparator.comparingLong(Thread::getId))
        .forEach(thread -> {
          text.append(
            String.format(
              "[%3d] %s\n",
              Long.valueOf(thread.getId()),
              thread.getName())
          );
        });
      return newFixedLengthResponse(text.toString());
    }

    @Override
    public Response serve(
      final IHTTPSession session)
    {
      LOG.info("serve: {}", session.getRemoteIpAddress());

      return switch (session.getUri()) {
        case "/tasks" -> showTasks();
        default -> showDefault();
      };
    }
  }
}
