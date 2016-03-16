package rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;

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
	private String username;
	private String password;

	public Ressources() {
		this.host = Constants.host;
		this.port = Constants.port;
		this.username = Constants.username;
		this.password = Constants.password;

		FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
		conf.setServerLanguageCode(FTPClientConfig.SYST_UNIX);

		client = new FTPClient();
		client.configure(conf);
	}

	private void connecter() {
		try {
			client.connect(host, port);
			client.login(username, password);

			if (!client.isConnected()) {
				throw new ConnectException("Utilisateur non connecte");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@GET
	@Produces("application/octet-stream")
	@Path("/get/{path}")
	public File get(@PathParam("path") String path) {
		File file = null;

		try {
			connecter();

			String[] paths = path.split("/");
			String filename = paths[paths.length - 1];

			file = new File(filename);
			FileOutputStream output = new FileOutputStream(file);

			client.retrieveFile(path, output);
			output.close();

			client.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return file;
	}

	@GET
	@Produces("text/html")
	@Path("/list{folder_path : (/folder_path)?}")
	public String list(@PathParam("folder_path") String folderPath) {
		System.out.println();
		FTPFile[] ftpFiles = null;

		try {
			connecter();

			if (client.isConnected()) {
				if (folderPath != null) {
					ftpFiles = client.listFiles(folderPath);
					folderPath += "/";
				} else {
					ftpFiles = client.listFiles("");
					folderPath = "";
				}

				client.disconnect();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		String html = "<ul>";

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
			html += "<a href=" + url + cmd + folderPath + file.getName() + ">" + filename + "</a>";
			html += " ";
			html += "<a href=" + url + "/delete/" + folderPath +
			file.getName() + ">" + "Delete" + "</a>";
			html += "</li>";
		}

		html += "</ul>";

		html += "<div>";
		html += "<form method='POST' action='" + url + "/upload' enctype='multipart/form-data'>\n";
		html += "Fichier<input type='file' name='file'><br>";
		html += "nom : <input type='hidden' name='path' value=''>";
		html += "<input type='text' name='name' /><br>\n";
		html += "<input type='submit' value='Envoyer'>\n";
		html += "</form> ";
		html += "</div>";

		return html;
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("text/html; charset=UTF-8")
	@Path("/upload")
	public String upload(@Multipart("file") InputStream fichier, @Multipart("name") String name,
			@Multipart("path") String path) throws IOException {

		connecter();
		System.out.println("coucou");

		if (client.isConnected()) {
			if(path != null) {
				path += "/";
			}
			System.out.println("Name : " + name);
			System.out.println("Fichier : " + fichier);
			client.storeFile(path + "/" + name, fichier);
			client.disconnect();
		}
		return "<h3>Fichier importé</h3>";
	}

	@GET
	@Produces("text/html; charset=UTF-8")
	@Path("/delete/{filepath}")
	public String deleteFile(@PathParam("filepath") String filepath) {

		connecter();

		if (client.isConnected()) {
			try {
				client.deleteFile(filepath);
				client.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return "<h3>Fichier supprimé</h3>";
	}
}
