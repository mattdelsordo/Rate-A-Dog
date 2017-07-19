package com.mdelsordo.rate_a_dog.model;

import android.content.Context;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.s3.model.Region;
import com.mdelsordo.rate_a_dog.util.Logger;

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

    private static final String COGNITO_POOL_ID = "us-east-1:838d53dc-d050-4e10-8856-0f1c6642c431";
    private static final Regions COGNITO_REGION = Regions.US_EAST_1;

    //accepts bytes and returns a collection of labels for the converted image
    public static Collection<Label> detectLabelsTest(byte[] bytes, Context context){
        AWSCredentials credentials;

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                COGNITO_POOL_ID, // Identity pool ID
                COGNITO_REGION // Region
        );
        credentials = credentialsProvider.getCredentials();

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

//        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
//                .standard()
//                .withRegion(Regions.US_WEST_2)
//                .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                .build();
        AmazonRekognition rekognitionClient = new AmazonRekognitionClient(credentials);

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withBytes(buffer))
                .withMaxLabels(MAX_LABELS)
                .withMinConfidence(MIN_CONFIDENCE);

        DetectLabelsResult result = rekognitionClient.detectLabels(request);
        List<Label> labels = result.getLabels();

//        System.out.println("Detected labels for image.");
//        for (Label label: labels) {
//            Logger.i(TAG, label.getName() + ": " + label.getConfidence().toString());
//        }

        return labels;
    }

    //searches collection of labels for instances of "dog"
    public static boolean containsDog(Collection<Label> labels){
        for(Label l : labels){
            if(l.getName().equals("Dog")) return true;
        }
        return false;
    }
}
