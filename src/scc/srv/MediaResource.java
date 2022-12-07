package scc.srv;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import jakarta.ws.rs.*;
import scc.utils.Hash;

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

		try {

			BinaryData data = BinaryData.fromBytes(contents);

			BlobContainerClient containerClient = new BlobContainerClientBuilder()
					.connectionString(storageConnectionString)
					.containerName(CONTAINER_NAME)
					.buildClient();

			BlobClient blob = containerClient.getBlobClient(key);

			blob.upload(data);

		} catch( Exception e) {
			e.printStackTrace();
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

		byte[] arr = null;
		try {
			// Get container client
			BlobContainerClient containerClient = new BlobContainerClientBuilder()
					.connectionString(storageConnectionString)
					.containerName(CONTAINER_NAME)
					.buildClient();

			BlobClient blob = containerClient.getBlobClient( id);

			BinaryData data = blob.downloadContent();

			arr = data.toBytes();

		} catch( Exception e) {
			e.printStackTrace();
		}
		if(arr == null) throw new ServiceUnavailableException();
		return arr;
	}

	/**
	 * Lists the ids of images stored.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String list() {
		List<String> arr = new ArrayList<>();
		try {
			// Get container client
			BlobContainerClient containerClient = new BlobContainerClientBuilder()
					.connectionString(storageConnectionString)
					.containerName(CONTAINER_NAME)
					.buildClient();

			// Get client to blob
			var blobs = containerClient.listBlobs();

			blobs.forEach(b -> arr.add(b.getName()));

		} catch( Exception e) {
			e.printStackTrace();
		}
		if(arr.isEmpty()) throw new ServiceUnavailableException();

		return arr.toString();
	}
}
