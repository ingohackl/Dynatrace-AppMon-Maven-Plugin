package com.dynatrace.diagnostics.automation.maven;

import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.servermanagement.ServerManagement;
import com.dynatrace.sdk.server.sessions.Sessions;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerManagement.class, DtRestartServer.class})
public class DtRestartServerTest extends AbstractDynatraceMojoTest<DtRestartServer> {
    private static final String RESTART_SERVER_GOAL_NAME = "restartServer";

    /** server sdk class used in tested mojo */
    private ServerManagement serverManagement;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.applyFreshMojo();

        serverManagement = spy(new ServerManagement(mojo.getDynatraceClient()));

        /** define responses */
        doReturn(true).when(serverManagement).restart();
        doThrow(new ServerConnectionException("message", new Exception())).when(serverManagement).shutdown();

        whenNew(ServerManagement.class).withAnyArguments().thenReturn(serverManagement);

        /** verify default values */
        assertThat(mojo.getRestart(), is(true));
    }

    @Override
    protected String getMojoGoalName() {
        return RESTART_SERVER_GOAL_NAME;
    }

    @Test
    public void testRestartServer() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setRestart(true);
            mojo.execute();
        } catch (Exception e) {
            fail(String.format("Exception shouldn't be thrown: %s", e.getMessage()));
        }
    }

    @Test
    public void testShutdownServer() throws Exception {
        this.applyFreshMojo();

        try {
            mojo.setRestart(false);
            mojo.execute();

            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e, instanceOf(MojoExecutionException.class));
        }
    }
}