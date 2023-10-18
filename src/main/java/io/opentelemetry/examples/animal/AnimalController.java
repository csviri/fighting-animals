/* Copyright (C) Red Hat 2023 */
package io.opentelemetry.examples.animal;

import static io.opentelemetry.examples.utils.Misc.fetchAnimal;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnimalController {
  private static final Map<String, String> SERVICES =
      Map.of(
          "mammals", "http://mammal-service:8081/getAnimal",
          "fish", "http://fish-service:8083/getAnimal");

  @Autowired private HttpServletRequest httpServletRequest;

  private final Counter battlesTotal;

  private final MeterRegistry registry;

  public AnimalController(MeterRegistry registry) {
    this.registry = registry;
    this.battlesTotal = this.registry.counter("battles.total");
    //    this.battlesTotal = Counter
    //            .builder("battles.total")
    //            .description("Total number of battle fought")
    //            .register(this.registry);
    // These next two lines switch on CPU & memory metrics for delivery through Micrometer
    new ProcessorMetrics().bindTo(this.registry);
    new JvmMemoryMetrics().bindTo(this.registry);
  }

  @GetMapping("/battle")
  public String makeBattle() throws IOException, InterruptedException {
    // Send the two requests and return the response body as the response
    var good = fetchRandomAnimal();
    var evil = fetchRandomAnimal();
    battlesTotal.increment();
    return "{ \"good\": \"" + good + "\", \"evil\": \"" + evil + "\" }";
  }

  private String fetchRandomAnimal() throws IOException, InterruptedException {
    List<String> keys = List.copyOf(SERVICES.keySet());
    var world = keys.get((int) (SERVICES.size() * Math.random()));
    var location = SERVICES.get(world);

    return fetchAnimal(world, location);
  }
}
