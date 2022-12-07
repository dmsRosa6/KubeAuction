package scc.srv;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import jakarta.ws.rs.*;
import scc.utils.Hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource
{
	String storageConnectionString = System.getenv("STORAGE_CONNECTION_STRING");
	private static final String CONTAINER_NAME = "images";
	/**
	 * Post a new image.The id of the image is its hash.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {

		String key = Hash.of(contents);

		if (key == null || contents == null){
			throw new RuntimeException();
		}
		try {
			File file = new File(key);
			file.createNewFile();
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(contents);
			outputStream.close();
		}catch (IOException e) {
		}

		return key;
	}

	/**
	 * Return the contents of an image. Throw an appropriate error message if
	 * id does not exist.
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) {


		if (id == null){
			throw new RuntimeException();
		}
		File file = new File(id);

		if(!file.exists()){
			throw new RuntimeException();
		}

		byte[] content = new byte[(int) file.length()];

		try
		{
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(content);
			fileInputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		if(content == null) throw new ServiceUnavailableException();

		return content;
	}

	/**
	 * Lists the ids of images stored.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String list() {
		File folder = new File(System.getProperty("user.dir"));
		File[] listOfFiles = folder.listFiles();
		List<String> list = new ArrayList();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				list.add(listOfFiles[i].getName());
			}
		}
		return list.toString();
	}
}
