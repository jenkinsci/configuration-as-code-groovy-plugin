package io.jenkins.plugins.cascgroovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;
import io.jenkins.plugins.casc.Attribute;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.Configurator;
import io.jenkins.plugins.casc.ConfiguratorException;
import io.jenkins.plugins.casc.RootElementConfigurator;
import io.jenkins.plugins.casc.impl.attributes.MultivaluedAttribute;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import static io.vavr.API.Try;
import static io.vavr.API.unchecked;


/**
 * @author <a href="mailto:tomasz.szandala@gmail.com">Tomasz Szandala</a>
 */
@Extension(optional = true, ordinal = -60) // Ordinal -60 Ensure it is loaded after JobDSL
@Restricted(NoExternalUse.class)
public class GroovyScriptCaller implements RootElementConfigurator<Boolean[]> {

    @Override
    @Nonnull
    public String getName() {
        return "groovy";
    }

    @Override
    public Class getTarget() {
        return Boolean[].class;
    }

    @Override
    @Nonnull
    public Set<Attribute<Boolean[],?>> describe() {
        return Collections.singleton(new MultivaluedAttribute("", GroovyScriptSource.class));
    }

    @Override
    public Boolean[] getTargetComponent(ConfigurationContext context) {
        return new Boolean[0]; // Doesn't really make sense
    }

    @Override
    @Nonnull
    public Boolean[] configure(CNode config, ConfigurationContext context) throws ConfiguratorException {
        final Configurator<GroovyScriptSource> c = context.lookup(GroovyScriptSource.class);
        return config.asSequence().stream()
            .map(source -> getActualValue(source, context))
            .map(source -> getScriptFromSource(source, context, c))
            .map(script -> Try(script::getScript).onSuccess(GroovyScriptCaller.this::runGroovyShell).isSuccess())
            .toArray(Boolean[]::new);
    }

    private CNode getActualValue(CNode source, ConfigurationContext context) {
        return unchecked(() -> source.asMapping().entrySet().stream().findFirst()).apply()
            .map(entry -> resolveSourceOrGetValue(entry, context))
            .orElse(source);
    }

    private CNode resolveSourceOrGetValue(Map.Entry<String, CNode> entry, ConfigurationContext context) {
        final Mapping m = new Mapping();
        m.put(
            entry.getKey(),
            context.getSecretSourceResolver().resolve(unchecked(() -> entry.getValue().asScalar().getValue()).apply())
        );
        return m;
    }

    private GroovyScriptSource getScriptFromSource(CNode source, ConfigurationContext context,
            Configurator<GroovyScriptSource> configurator) {
        return unchecked(() ->
                Try(() -> configurator.configure(source, context))
                        .getOrElseThrow(t -> new ConfiguratorException(this,
                                "Failed to retrieve groovy script", t))).apply();
    }

    private void runGroovyShell(String script) {
        final GroovyShell s = new GroovyShell(Jenkins.get().getPluginManager().uberClassLoader, new Binding());
        unchecked(() -> s.run(script, "ConfigurationAsCodeGroovy", new ArrayList()));
    }


    @Override
    public Boolean[] check(CNode config, ConfigurationContext context) throws ConfiguratorException {
        // Any way to dry-run a Groovy script ?
        return new Boolean[0];
    }

    @Nonnull
    @Override
    public List<Configurator<Boolean[]>> getConfigurators(ConfigurationContext context) {
        return Collections.singletonList(context.lookup(GroovyScriptSource.class));
    }

    @CheckForNull
    @Override
    public CNode describe(Boolean[] instance, ConfigurationContext context) throws Exception {
        return null;
    }

}
