package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import javassist.bytecode.ByteArray;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Iterator;

public class S3Store implements BlobStore {

    private final AmazonS3 s3;
    private final String bucketName;
    private final Tika tika = new Tika();

    public S3Store(AmazonS3 s3Client, String s3BucketName) {
        this.bucketName = s3BucketName;
        this.s3 = s3Client;
    }

    @Override
    public void put(Blob blob) throws IOException {
        s3.putObject(new PutObjectRequest(bucketName, blob.name, blob.inputStream, new ObjectMetadata()));
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {

        S3Object object = s3.getObject(bucketName, name);
        S3ObjectInputStream objectContent = object.getObjectContent();
        byte[] bytes = IOUtils.toByteArray(objectContent);
        Blob blob = new Blob(name, new ByteArrayInputStream(bytes), tika.detect(object.getObjectContent()));

        return Optional.of(blob);
    }

    @Override
    public void deleteAll() {
        ObjectListing objectListing = s3.listObjects(bucketName);
        for (Iterator<?> iterator = objectListing.getObjectSummaries().iterator(); iterator.hasNext();){
            S3ObjectSummary summary = (S3ObjectSummary)iterator.next();
            s3.deleteObject(bucketName, summary.getKey());
        }
    }
}
