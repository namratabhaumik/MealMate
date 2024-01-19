package com.groupeleven.mealmate.InventoryManagement;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.groupeleven.mealmate.InventoryManagement.models.UserInventory;
import com.groupeleven.mealmate.InventoryManagement.models.UserInventoryItem;
import com.groupeleven.mealmate.InventoryManagement.utils.InventoryLoader;

@RunWith(RobolectricTestRunner.class)
public class InventoryLoaderTest {
    private InventoryLoader inventoryLoader;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private Task<QuerySnapshot> mockQuerySnapshotTask;

    @Mock
    private Task<DocumentReference> mockDocumentReferenceTask;

    @Mock
    private CollectionReference mockCollectionReference;

    @Mock
    private QuerySnapshot mockQuerySnapshot;

    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    @Mock
    private DocumentReference mockDocumentReference;

    @Mock
    private Query mockQuery;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        inventoryLoader = new InventoryLoader(mockFirestore);

        // For Mocking Firestore and its related classes
        when(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference);
        when(mockCollectionReference.whereEqualTo(anyString(), anyString())).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockQuerySnapshotTask);

        // Adding at least one "DocumentSnapshot" to get the first index result, i-e: "get(0)"
        List<DocumentSnapshot> documentSnapshots = new ArrayList<>();
        documentSnapshots.add(mockDocumentSnapshot);
        when(mockQuerySnapshot.getDocuments()).thenReturn(documentSnapshots);

        // For Mocking Async Tasks (Android GMS)
        when(mockQuerySnapshotTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQuerySnapshotTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<QuerySnapshot, Task<Boolean>> continuation =
                    invocation.getArgument(0);
            return continuation.then(mockQuerySnapshotTask);
        });
    }

    @Test
    public void uploadGroceryListToInventory_NullLoadedGroceryList_ReturnsFalse(){
        boolean taskResult = inventoryLoader.uploadGroceryListToInventory(
                "userId", null, true)
                .getResult();
        assertFalse(taskResult);
    }

    @Test
    public void uploadGroceryListToInventory_EmptyLoadedGroceryList_ReturnsFalse(){
        boolean taskResult = inventoryLoader.uploadGroceryListToInventory(
                "userId", Collections.emptyMap(), true)
                .getResult();
        assertFalse(taskResult);
    }

    @Test
    public void uploadGroceryListToInventory_UserInventoriesTaskNotSuccessful_ReturnsFalse(){
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(false);

        Map<String, Float> mockGroceryList = new HashMap<>();
        mockGroceryList.put("Item1", 10.0f);
        mockGroceryList.put("Item2", 20.0f);

        boolean taskResult = inventoryLoader.uploadGroceryListToInventory(
                "userId", mockGroceryList, true)
                .getResult();

        assertFalse(taskResult);
    }

    @Test
    public void uploadGroceryListToInventory_WithNoExistingUserInventory_ReturnsTrue() {
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        when(mockQuerySnapshot.isEmpty()).thenReturn(true);

        when(mockDocumentReferenceTask.isSuccessful()).thenReturn(true);

        when(mockCollectionReference.add(any())).thenReturn(mockDocumentReferenceTask);

        Map<String, Float> mockGroceryList = new HashMap<>();
        mockGroceryList.put("Item1", 10.0f);
        mockGroceryList.put("Item2", 20.0f);

        inventoryLoader.uploadGroceryListToInventory(
                "userId", mockGroceryList, true);

        verify(mockDocumentReferenceTask).continueWith(any());
    }

    @Test
    public void uploadGroceryListToInventory_WithExistingUserInventoryAndCheckInventory_ReturnsTrue() {
        Task<Void> mockVoidTask = mock(Task.class);
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        when(mockQuerySnapshot.isEmpty()).thenReturn(false);

        List<UserInventoryItem> existingInventoryItems = new ArrayList<>();
        existingInventoryItems.add(new UserInventoryItem("Item1",
                5.0f));
        UserInventory existingInventory = new UserInventory("userId",
                existingInventoryItems);
        when(mockDocumentSnapshot.toObject(UserInventory.class)).thenReturn(existingInventory);

        when(mockCollectionReference.document(any())).thenReturn(mockDocumentReference);
        when(mockDocumentReference.set(existingInventory)).thenReturn(mockVoidTask);

        Map<String, Float> mockGroceryList = new HashMap<>();
        mockGroceryList.put("Item1", 10.0f);
        mockGroceryList.put("Item2", 20.0f);

        inventoryLoader.uploadGroceryListToInventory(
                "userId", mockGroceryList, true);

        assertEquals(mockGroceryList.size(), existingInventory.getInventoryItems().size());

        List<String> addedItemNames = new ArrayList<>(mockGroceryList.keySet());
        List<String> existingItemNames = existingInventory.getInventoryItems().stream()
                .map(UserInventoryItem::getIngredientName)
                .collect(Collectors.toList());

        assertTrue(existingItemNames.containsAll(addedItemNames));

        verify(mockVoidTask).continueWith(any());
    }

    @Test
    public void uploadGroceryListToInventory_ExistingUserInventoryAndNotCheckInventory_ReturnsTrue() {
        Task<Void> mockVoidTask = mock(Task.class);
        when(mockQuerySnapshotTask.isSuccessful()).thenReturn(true);
        when(mockQuerySnapshot.isEmpty()).thenReturn(false);

        List<UserInventoryItem> existingInventoryItems = new ArrayList<>();
        existingInventoryItems.add(new UserInventoryItem("Item1",
                5.0f));
        UserInventory existingInventory = new UserInventory("userId",
                existingInventoryItems);
        when(mockDocumentSnapshot.toObject(UserInventory.class)).thenReturn(existingInventory);

        when(mockCollectionReference.document(any())).thenReturn(mockDocumentReference);
        when(mockDocumentReference.set(existingInventory)).thenReturn(mockVoidTask);

        Map<String, Float> mockGroceryList = new HashMap<>();
        mockGroceryList.put("Item1", 10.0f);
        mockGroceryList.put("Item2", 20.0f);

        inventoryLoader.uploadGroceryListToInventory(
                "userId", mockGroceryList, false);

        assertEquals(mockGroceryList.size(), existingInventory.getInventoryItems().size());

        List<String> addedItemNames = new ArrayList<>(mockGroceryList.keySet());
        List<String> existingItemNames = existingInventory.getInventoryItems().stream()
                .map(UserInventoryItem::getIngredientName)
                .collect(Collectors.toList());

        assertTrue(existingItemNames.containsAll(addedItemNames));

        verify(mockVoidTask).continueWith(any());
    }
}