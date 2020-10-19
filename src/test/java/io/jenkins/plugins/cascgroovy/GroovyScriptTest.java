package io.jenkins.plugins.cascgroovy;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import jenkins.model.Jenkins;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GroovyScriptTest {
    @Rule
    public JenkinsConfiguredWithCodeRule j = new JenkinsConfiguredWithCodeRule();

    @Test
    @ConfiguredWithCode("casc.yaml")
    @Ignore
    public void configure() throws Exception {
        Jenkins jenkins = Jenkins.get();
        assertThat(jenkins.getSystemMessage(), is("Hello World"));
    }
}
