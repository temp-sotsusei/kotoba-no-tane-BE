package io.github.tempsotsusei.kotobanotane.interfaces.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healthz")
public class HealthController {

  @GetMapping
  public Map<String, String> getHealth() {
    return Map.of("status", "ok");
  }
}
