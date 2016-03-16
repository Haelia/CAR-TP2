package rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

@Path("/commandes")
public class Ressources {

	final static private String url = "http://localhost:8080/rest/tp2/commandes";
	private FTPClient client;
	private String host;
	private int port;
	private boolean authentified = false;

	public Ressources() {
		this.host = Constants.host;
		this.port = Constants.port;

		FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
		conf.setServerLanguageCode(FTPClientConfig.SYST_UNIX);

		client = new FTPClient();
		client.configure(conf);
	}

	public boolean isAuthentified() {
		return authentified;
	}

	@GET
	@Produces("text/html; charset=UTF-8")
	@Path("")
	public String login() {
		String html = "<h3>Login</h3>";

		html += "<div>";
		html += "<form method='POST' action='" + url + "/connexion' enctype='multipart/form-data'>\n";
		html += "<input type='text' name='username' /><br>\n";
		html += "<input type='password' name='password' /><br>\n";
		html += "<input type='submit' value='Connexion'>\n";
		html += "</form> ";
		html += "</div>";

		return html;
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("text/html; charset=UTF-8")
	@Path("/connexion")
	public String connexion(@Multipart("username") String username, @Multipart("password") String password)
			throws IOException {

		try {
			client.connect(host, port);
			this.authentified = client.login(username, password);

			if (isAuthentified()) {
			return "<h3>Connecté</h3></br><a href='" + url + "/list'>Aller au répertoire du serveur FTP</a>";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		client.disconnect();
		return "<h3>Erreur d'authentification</h3>";
	}

	@GET
	@Produces("application/octet-stream")
	@Path("/get{path : (/path)?}")
	public File get(@PathParam("path") String path) {
		File file = null;

		if (isAuthentified()) {
			try {
				String[] paths = path.split("/");
				String filename = paths[paths.length - 1];

				file = new File(filename);
				FileOutputStream output = new FileOutputStream(file);

				client.retrieveFile(path, output);
				output.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return file;
	}

	@GET
	@Produces("text/html")
	@Path("/list{folder_path : (/folder_path)?}")
	public String list(@PathParam("folder_path") String folderPath) {
		FTPFile[] ftpFiles = null;

		String html = "";

		if (isAuthentified()) {
			try {
				if (folderPath != null) {
					ftpFiles = client.listFiles(folderPath);
					folderPath += "/";
				} else {
					ftpFiles = client.listFiles("");
					folderPath = "";
				}

			} catch (final Exception e) {
				e.printStackTrace();
			}

			html += "<ul>";

			for (FTPFile file : ftpFiles) {
				String cmd;
				String filename = file.getName();
				if (file.isDirectory()) {
					cmd = "/list/";
					filename += "/";
				} else {
					cmd = "/get/";
				}

				html += "<li>";
				html += "<a href='" + url + cmd + folderPath + file.getName() + "'>" + filename + "</a>";
				html += " ";

				if (!file.isDirectory())
					html += "<a href='" + url + "/delete/" + folderPath + file.getName() + "'>" + "Delete" + "</a>";

				html += "</li>";
			}

			html += "</ul>";

			html += "<div>";
			html += "<form method='POST' action='" + url + "/upload' enctype='multipart/form-data'>\n";
			html += "Fichier<input type='file' name='file'><br>";
			html += "nom : <input type='hidden' name='path' value='" + folderPath + "'>";
			html += "<input type='text' name='name' /><br>\n";
			html += "<input type='submit' value='Envoyer'>\n";
			html += "</form> ";
			html += "</div>";
		}

		return html;
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("text/html; charset=UTF-8")
	@Path("/upload")
	public String upload(@Multipart("file") InputStream fichier, @Multipart("name") String name,
			@Multipart("path") String path) throws IOException {
		String html = "";

		if (isAuthentified()) {
			client.storeFile(path + name, fichier);
			html = "<h3>Fichier importé</h3>";
		}

		return html;
	}

	@GET
	@Produces("text/html; charset=UTF-8")
	@Path("/delete{filepath : (/filepath)?}")
	public String deleteFile(@PathParam("filepath") String filepath) {
		String html = "";

		if (isAuthentified()) {
			try {
				client.deleteFile(filepath);
			} catch (IOException e) {
				e.printStackTrace();
			}

			html = "<h3>Fichier supprimé</h3>";
		}

		return html;
	}
}
