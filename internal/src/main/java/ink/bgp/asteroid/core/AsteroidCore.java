package ink.bgp.asteroid.core;

import ink.bgp.asteroid.api.Asteroid;
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
import org.apache.ivy.util.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public final class AsteroidCore implements Asteroid {
  private final @NotNull Path dataDirectory = Paths.get("asteroid");

  @Getter(lazy = true)
  private final @NotNull Instrumentation instrumentation = loadInstrumentation();

  @Getter(lazy = true)
  private final @NotNull IvySettings ivySettings = loadIvySettings();

  @Getter(lazy = true)
  private final @NotNull DefaultWorkspaceModuleDescriptor serverProject = loadServerProject();

  private @Nullable ResolveReport resolveReport;

  public AsteroidCore() {
    Asteroid.$set$instance(this);
  }

  @SneakyThrows
  private @NotNull Instrumentation loadInstrumentation() {
    return  (Instrumentation) Class.forName("ink.bgp.asteroid.loader.AsteroidMain")
        .getMethod("instrumentation")
        .invoke(null);
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

    final Path ivysettingsPath = dataDirectory.resolve("ivysettings.xml");
    if (Files.exists(ivysettingsPath) && !Files.isDirectory(ivysettingsPath)) {
      settings.load(ivysettingsPath.toUri().toURL());
    }

    return settings;
  }

  @SneakyThrows
  private @NotNull DefaultWorkspaceModuleDescriptor loadServerProject() {
    final IvySettings settings = ivySettings();

    final ModuleDescriptor buildServerProject = XmlModuleDescriptorParser.getInstance()
        .parseDescriptor(settings, dataDirectory.resolve("build.xml").toUri().toURL(), false);

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
  public @Nullable File getFile(
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
      return artifactReport.getLocalFile();
    }
    return null;
  }

  @Override
  @SneakyThrows
  public void injectSystemClassPath() {
    final ResolveReport resolveReport = resolveReport();

    Message.info(":: apply system artifact :: " + resolveReport.getResolveId());
    final ConfigurationResolveReport systemConfigurationReport = resolveReport.getConfigurationReport("system");
    if (systemConfigurationReport == null) {
      return;
    }
    final ArtifactDownloadReport[] systemArtifactsReports = systemConfigurationReport.getAllArtifactsReports();
    if (systemArtifactsReports == null) {
      return;
    }
    for (final ArtifactDownloadReport artifactsReport : systemArtifactsReports) {
      final File artifactFile = artifactsReport.getLocalFile();
      if (!artifactFile.getName().endsWith(".jar")) {
        Message.info("\tskip non-jar artifact: " + artifactFile);
        continue;
      }
      Message.info("\tapply system library artifact: " + artifactFile);
      instrumentation().appendToSystemClassLoaderSearch(new java.util.jar.JarFile(artifactFile));
    }
  }
}
