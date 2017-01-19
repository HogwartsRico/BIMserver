package org.bimserver.database.actions;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.bimserver.BimServer;
import org.bimserver.BimserverDatabaseException;
import org.bimserver.database.BimserverLockConflictException;
import org.bimserver.database.DatabaseSession;
import org.bimserver.interfaces.objects.SPluginInformation;
import org.bimserver.models.log.AccessMethod;
import org.bimserver.plugins.LocalMavenPluginBundle;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallPluginBundleFromBytes extends BimDatabaseAction<Void> {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstallPluginBundleFromBytes.class);
	private BimServer bimServer;
	private byte[] data;

	public InstallPluginBundleFromBytes(DatabaseSession databaseSession, AccessMethod accessMethod, BimServer bimServer, byte[] data) {
		super(databaseSession, accessMethod);
		this.bimServer = bimServer;
		this.data = data;
	}

	@Override
	public Void execute() throws UserException, BimserverLockConflictException, BimserverDatabaseException, ServerException {
		LocalMavenPluginBundle mavenPluginBundle = new LocalMavenPluginBundle(data);

		try {
			List<SPluginInformation> pluginInformationFromPluginFile = bimServer.getPluginManager().getPluginInformationFromJar(new ByteArrayInputStream(data));
			for (SPluginInformation sPluginInformation : pluginInformationFromPluginFile) {
				sPluginInformation.setEnabled(true);
				sPluginInformation.setInstallForAllUsers(true);
				sPluginInformation.setInstallForNewUsers(true);
			}
			bimServer.getPluginManager().install(mavenPluginBundle, null, false);
		} catch (Exception e) {
			LOGGER.error("", e);
			throw new UserException(e);
		}

		return null;
	}
}
