package com.mdelsordo.rate_a_dog.util;

import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

/**
 * Created by mdelsord on 7/17/17.
 *
 * Handles sending image to the server and retrieving the list of labels
 */

public class AWSUtil {
    private static String TAG = "AWSUtil";

    private static final float MIN_CONFIDENCE = 80F;
    private static final int MAX_LABELS = 10;

    //accepts image and retuns a ByteBuffer
//    public static ByteBuffer imageToBytes(String image){
//
//    }

    //accepts bytes and returns a collection of labels for the converted image
    public static Collection<Label> detectLabelsTest(ByteBuffer bytes) throws AmazonRekognitionException{
        AWSCredentials credentials;
        try {
            //!!!!TODO: This will not work I need to figure out how to send the ACTUAL credentials!!!!
            credentials = new ProfileCredentialsProvider("AdminUser").getCredentials();
        } catch(Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
                    + "Please make sure that your credentials file is at the correct "
                    + "location (/Users/userid/.aws/credentials), and is in a valid format.", e);
        }

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
                .standard()
                .withRegion(Regions.US_WEST_2)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withBytes(bytes))
                .withMaxLabels(MAX_LABELS)
                .withMinConfidence(MIN_CONFIDENCE);

        DetectLabelsResult result = rekognitionClient.detectLabels(request);
        List<Label> labels = result.getLabels();

        System.out.println("Detected labels for image.");
        for (Label label: labels) {
            Logger.i(TAG, label.getName() + ": " + label.getConfidence().toString());
        }

        return labels;
    }

    //searches collection of labels for instances of "dog"
    //TODO: i am 0% confident thats how that works at all
    public static boolean containsDog(Collection<Label> labels){
        return labels.contains("dog");
    }
}
