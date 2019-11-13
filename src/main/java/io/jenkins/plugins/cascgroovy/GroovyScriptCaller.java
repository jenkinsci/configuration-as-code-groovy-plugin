package io.jenkins.plugins.cascgroovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;
import io.jenkins.plugins.casc.*;
import io.jenkins.plugins.casc.impl.attributes.MultivaluedAttribute;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.*;

import static io.vavr.API.Try;
import static io.vavr.API.unchecked;


/**
 * @author <a href="mailto:tomasz.szandala@gmail.com">Tomasz Szandala</a>
 */
@Extension(optional = true, ordinal = -50)
@Restricted(NoExternalUse.class)
public class GroovyScriptCaller implements RootElementConfigurator<Boolean[]> {

    @Override
    public String getName() {
        return "groovy";
    }

    @Override
    public Class getTarget() {
        return Boolean[].class;
    }

    @Override
    public Set<Attribute<Boolean[],?>> describe() {
        return Collections.singleton(new MultivaluedAttribute("", GroovyScriptSource.class));
    }

    @Override
    public Boolean[] getTargetComponent(ConfigurationContext context) {
        return new Boolean[0]; // Doesn't really make sense
    }

    @Override
    public Boolean[] configure(CNode config, ConfigurationContext context) throws ConfiguratorException {
        final Configurator<GroovyScriptSource> c = context.lookup(GroovyScriptSource.class);
        return config.asSequence().stream()
            .map(source -> getActualValue(source, context))
            .map(source -> Try(() -> c.configure(source, context).getScript())
                                .onSuccess(GroovyScriptCaller.this::runGroovyShell)
                                .isSuccess())
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
            SecretSourceResolver.resolve(context, unchecked(() -> entry.getValue().asScalar().getValue()).apply())
        );
        return m;
    }

    private void runGroovyShell(String script) {
        final GroovyShell s = new GroovyShell(Jenkins.getActiveInstance().getPluginManager().uberClassLoader, new Binding());
        unchecked(() -> s.run(script, "Configuration-as-Code-Groovy", new ArrayList()));
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
