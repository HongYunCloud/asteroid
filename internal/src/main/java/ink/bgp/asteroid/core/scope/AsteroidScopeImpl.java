package ink.bgp.asteroid.core.scope;

import ink.bgp.asteroid.api.scope.AsteroidDependency;
import ink.bgp.asteroid.api.scope.AsteroidScope;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.*;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ConfigurationResolveReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsteroidScopeImpl implements AsteroidScope {
  private static final @NotNull Path dataDirectory = Paths.get("asteroid");

  private final @NotNull String scopeName;

  @Getter(lazy = true)
  private final @NotNull IvySettings ivySettings = loadIvySettings();

  @Getter(lazy = true)
  private final @NotNull DefaultWorkspaceModuleDescriptor serverProject = resolveScope();

  private @Nullable ResolveReport resolveReport;

  public AsteroidScopeImpl(final @NotNull String scopeName) {
    this.scopeName = scopeName;
  }

  private @NotNull Path configPath(final @NotNull String fileName) {
    if (scopeName.isEmpty()) {
      return dataDirectory.resolve(fileName);
    } else {
      final Path scopedPath = dataDirectory.resolve(scopeName + "-" + fileName);

      if("ivysettings.xml".equals(fileName) && !(Files.exists(scopedPath) && !Files.isDirectory(scopedPath))) {
        return dataDirectory.resolve(fileName);
      }
      return dataDirectory.resolve(scopeName + "-" + fileName);
    }
  }

  @SneakyThrows
  private @NotNull IvySettings loadIvySettings() {
    // fix: https://github.com/com-lihaoyi/Ammonite/commit/3668fb4c4954593f14e8a2b7b64cd88f61bfe985
    final IvySettings settings = new IvySettings() {
      @Override
      public DependencyResolver getResolver(String resolverName) {
        if (getResolverNames().contains(resolverName)) {
          return super.getResolver(resolverName);
        } else {
          return null;
        }
      }
    };

    settings.setCheckUpToDate(false);
    settings.setDefaultCache(dataDirectory.resolve("cache").toFile());

    final Path globalIvysettingsPath = configPath("ivysettings.xml");
    if (Files.exists(globalIvysettingsPath) && !Files.isDirectory(globalIvysettingsPath)) {
      settings.load(globalIvysettingsPath.toUri().toURL());
    }

    return settings;
  }

  @SneakyThrows
  private @NotNull DefaultWorkspaceModuleDescriptor resolveScope() {
    final IvySettings settings = ivySettings();

    final ModuleDescriptor buildServerProject = XmlModuleDescriptorParser.getInstance()
        .parseDescriptor(settings, configPath("build.xml").toUri().toURL(), false);

    final DefaultWorkspaceModuleDescriptor serverProject = new DefaultWorkspaceModuleDescriptor(
        buildServerProject.getResolvedModuleRevisionId(),
        buildServerProject.getStatus(),
        buildServerProject.getPublicationDate(),
        buildServerProject.isDefault());

    for (final Configuration configuration : buildServerProject.getConfigurations()) {
      serverProject.addConfiguration(configuration);
    }
    for (final DependencyDescriptor dependency : buildServerProject.getDependencies()) {
      serverProject.addDependency(dependency);
    }
    for (final ExcludeRule excludeRule : buildServerProject.getAllExcludeRules()) {
      serverProject.addExcludeRule(excludeRule);
    }
    return serverProject;
  }

  @SneakyThrows
  private @NotNull ResolveReport loadResolveReport() {
    final IvySettings settings = ivySettings();
    final Ivy ivy = Ivy.newInstance(settings);
    final DefaultWorkspaceModuleDescriptor serverProject = serverProject();

    final ResolveOptions resolveOptions = new ResolveOptions();
    resolveOptions.setRefresh(false);
    resolveOptions.setCheckIfChanged(true);

    return ivy.resolve(serverProject, resolveOptions);
  }

  public @NotNull ResolveReport resolveReport() {
    final ResolveReport resolveReport = this.resolveReport;
    if (resolveReport == null) {
      throw new IllegalStateException("resolve report not loaded");
    }
    return resolveReport;
  }

  @Override
  @SneakyThrows
  public void run() {
    resolveReport = loadResolveReport();
  }

  @Override
  public void addDependency(
      final @NotNull String configuration,
      final @NotNull String group,
      final @NotNull String module,
      final @NotNull String version,
      final @Nullable String targetConfiguration) {
    final DefaultWorkspaceModuleDescriptor serverProject = serverProject();
    final DefaultDependencyDescriptor dependencyDescriptor = new DefaultDependencyDescriptor(
        ModuleRevisionId.newInstance(group, module, version), true);
    dependencyDescriptor.addDependencyConfiguration(configuration, targetConfiguration == null ? "runtime" : targetConfiguration);
    serverProject.addDependency(dependencyDescriptor);
  }

  @Override
  public @NotNull List<@NotNull AsteroidDependency> collectDependencies(final @NotNull String configuration) {
    final ResolveReport resolveReport = resolveReport();
    final ConfigurationResolveReport configurationReport = resolveReport.getConfigurationReport(configuration);
    if (configurationReport == null) {
      return Collections.emptyList();
    }
    final ArtifactDownloadReport[] artifactReports = configurationReport.getAllArtifactsReports();
    if(artifactReports == null) {
      return Collections.emptyList();
    }
    final List<AsteroidDependency> result = new ArrayList<>(artifactReports.length);
    for (final ArtifactDownloadReport artifactReport : artifactReports) {
      result.add(new AsteroidDependencyImpl(artifactReport));
    }
    return result;
  }

  @Override
  public @Nullable AsteroidDependency getDependency(
      final @NotNull String configuration,
      final @NotNull String group,
      final @NotNull String module,
      final @NotNull String version,
      final @Nullable String targetConfiguration) {
    final ResolveReport resolveReport = resolveReport();
    final ConfigurationResolveReport configurationReport = resolveReport.getConfigurationReport(configuration);
    if (configurationReport == null) {
      return null;
    }
    final ArtifactDownloadReport[] artifactReports = configurationReport.getAllArtifactsReports();
    if(artifactReports == null) {
      return null;
    }
    for (ArtifactDownloadReport artifactReport : artifactReports) {
      final Artifact artifact = artifactReport.getArtifact();
      if (!group.equals(artifact.getModuleRevisionId().getOrganisation())) {
        continue;
      }
      if (!module.equals(artifact.getModuleRevisionId().getOrganisation())) {
        continue;
      }
      if (!version.equals(artifact.getModuleRevisionId().getRevision())) {
        continue;
      }
      if (targetConfiguration != null && !targetConfiguration.equals(artifact.getExt())) {
        continue;
      }
      return new AsteroidDependencyImpl(artifactReport);
    }
    return null;
  }
}
