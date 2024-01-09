package ink.bgp.asteroid.core.scope;

import ink.bgp.asteroid.api.scope.AsteroidDependency;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class AsteroidDependencyImpl implements AsteroidDependency {
  private final @NotNull ArtifactDownloadReport report;

  public AsteroidDependencyImpl(final @NotNull ArtifactDownloadReport report) {
    this.report = report;
  }

  @Override
  public @NotNull File file() {
    return report.getLocalFile();
  }
}
