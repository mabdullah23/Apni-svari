package com.example.apni_svari.data;

import android.telephony.SmsManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.apni_svari.models.Car;
import com.example.apni_svari.models.HistoryCar;
import com.example.apni_svari.models.Proposal;
import com.example.apni_svari.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FirestoreRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface CarsCallback {
        void onLoaded(List<Car> cars);
    }

    public interface ProposalsCallback {
        void onLoaded(List<Proposal> proposals);
    }

    public interface HistoryCarsCallback {
        void onLoaded(List<HistoryCar> historyCars);
    }

    public interface UserCallback {
        void onLoaded(@Nullable User user);
    }

    public interface OperationCallback {
        void onComplete(boolean success, @Nullable String errorMessage);
    }

    public void fetchUserById(String userId, UserCallback callback) {
        if (TextUtils.isEmpty(userId)) {
            callback.onLoaded(null);
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(snapshot -> callback.onLoaded(mapUser(snapshot)))
                .addOnFailureListener(e -> callback.onLoaded(null));
    }

    public void fetchCarsForBuyer(String currentUserId, CarsCallback callback) {

        db.collection("cars").get()
                .addOnSuccessListener(snapshot -> {

                    List<Car> cars = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Car car = mapCar(doc);

                        String ownerId = car.getOwnerId();

                        if (ownerId == null || !ownerId.equals(currentUserId)) {

                            cars.add(car);
                        }
                    }

                    enrichCarsWithOwnerNames(cars, callback);
                })
                .addOnFailureListener(e -> callback.onLoaded(new ArrayList<>()));
    }

    public void fetchProposalsForOwner(String ownerId, ProposalsCallback callback) {
        if (TextUtils.isEmpty(ownerId)) {
            callback.onLoaded(new ArrayList<>());
            return;
        }

        db.collection("proposals")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Proposal> proposals = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Proposal proposal = mapProposal(doc);
                        if (proposal != null) {
                            proposals.add(proposal);
                        }
                    }
                    enrichProposalsWithBuyerNames(proposals, callback);
                })
                .addOnFailureListener(e -> callback.onLoaded(new ArrayList<>()));
    }

    public void fetchHistoryCarsForBuyer(String buyerId, HistoryCarsCallback callback) {
        if (TextUtils.isEmpty(buyerId)) {
            callback.onLoaded(new ArrayList<>());
            return;
        }

        db.collection("historycars")
                .whereEqualTo("buyerId", buyerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<HistoryCar> historyCars = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        HistoryCar historyCar = mapHistoryCar(doc);
                        if (historyCar != null) {
                            historyCars.add(historyCar);
                        }
                    }
                    callback.onLoaded(historyCars);
                })
                .addOnFailureListener(e -> callback.onLoaded(new ArrayList<>()));
    }

    public void fetchHistoryCarsForSeller(String ownerId, HistoryCarsCallback callback) {
        if (TextUtils.isEmpty(ownerId)) {
            callback.onLoaded(new ArrayList<>());
            return;
        }

        db.collection("historycars")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<HistoryCar> historyCars = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        HistoryCar historyCar = mapHistoryCar(doc);
                        if (historyCar != null) {
                            historyCars.add(historyCar);
                        }
                    }
                    callback.onLoaded(historyCars);
                })
                .addOnFailureListener(e -> callback.onLoaded(new ArrayList<>()));
    }

    public void createProposal(Proposal proposal, OperationCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("carId", proposal.getCarId());
        data.put("buyerId", proposal.getBuyerId());
        data.put("buyerName", proposal.getBuyerName());
        data.put("ownerId", proposal.getOwnerId());
        data.put("proposedPrice", proposal.getProposedPrice());
        data.put("carModel", proposal.getCarModel());
        data.put("status", proposal.getStatus() == null ? "pending" : proposal.getStatus());
        data.put("timestamp", proposal.getTimestamp() == 0L ? System.currentTimeMillis() : proposal.getTimestamp());

        db.collection("proposals")
                .whereEqualTo("carId", proposal.getCarId())
                .whereEqualTo("buyerId", proposal.getBuyerId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        String proposalId = snapshot.getDocuments().get(0).getId();
                        db.collection("proposals").document(proposalId)
                                .update("proposedPrice", proposal.getProposedPrice())
                                .addOnSuccessListener(unused -> callback.onComplete(true, null))
                                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
                    } else {
                        db.collection("proposals").add(data)
                                .addOnSuccessListener(doc -> callback.onComplete(true, null))
                                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }

    public void acceptProposal(Proposal proposal, OperationCallback callback) {
        if (TextUtils.isEmpty(proposal.getId())) {
            callback.onComplete(false, "Missing proposal id");
            return;
        }

        db.collection("proposals")
                .whereEqualTo("carId", proposal.getCarId())
                .get()
                .addOnSuccessListener(snapshot -> db.collection("cars").document(proposal.getCarId()).get()
                        .addOnSuccessListener(carSnapshot -> {
                            if (!carSnapshot.exists()) {
                                callback.onComplete(false, "Car not found");
                                return;
                            }

                            Car car = mapCar(carSnapshot);
                            WriteBatch batch = db.batch();
                            for (QueryDocumentSnapshot doc : snapshot) {
                                batch.delete(db.collection("proposals").document(doc.getId()));
                            }
                            batch.delete(db.collection("cars").document(proposal.getCarId()));
                            batch.set(db.collection("historycars").document(), buildHistoryCarData(car, proposal));

                            batch.commit()
                                    .addOnSuccessListener(unused -> {
                                        addMessageAndNotifyBuyer(proposal, "accepted");
                                        callback.onComplete(true, null);
                                    })
                                    .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
                        })
                        .addOnFailureListener(e -> callback.onComplete(false, e.getMessage())))
                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }

    public void rejectProposal(Proposal proposal, OperationCallback callback) {
        if (TextUtils.isEmpty(proposal.getId())) {
            callback.onComplete(false, "Missing proposal id");
            return;
        }

        db.collection("proposals").document(proposal.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    addMessageAndNotifyBuyer(proposal, "rejected");
                    callback.onComplete(true, null);
                })
                .addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }

    private void addMessageAndNotifyBuyer(Proposal proposal, String finalStatus) {
        Map<String, Object> message = new HashMap<>();
        message.put("carId", proposal.getCarId());
        message.put("buyerId", proposal.getBuyerId());
        message.put("ownerId", proposal.getOwnerId());
        message.put("carModel", proposal.getCarModel());
        message.put("proposedPrice", proposal.getProposedPrice());
        message.put("status", finalStatus);
        message.put("text", finalStatus.equals("accepted")
                ? "Your proposal for " + proposal.getCarModel() + " was accepted."
                : "Your proposal for " + proposal.getCarModel() + " was rejected.");
        message.put("timestamp", System.currentTimeMillis());

        db.collection("messages").add(message)
                .addOnSuccessListener(docRef -> {
                    sendSmsToBuyer(proposal, finalStatus);
                })
                .addOnFailureListener(e -> {
                });
    }

    private Map<String, Object> buildHistoryCarData(Car car, Proposal proposal) {
        Map<String, Object> history = new HashMap<>();
        history.put("carId", car.getId());
        history.put("buyerId", proposal.getBuyerId());
        history.put("buyerName", proposal.getBuyerName());
        history.put("ownerId", car.getOwnerId());
        history.put("ownerName", car.getOwnerName());
        history.put("ownerPhone", car.getOwnerPhone());
        history.put("carName", car.getName());
        history.put("name", car.getName());
        history.put("model", car.getModel());
        history.put("price", car.getPrice());
        history.put("acceptedPrice", proposal.getProposedPrice());
        history.put("imageBase64", car.getImageBase64());
        history.put("imageUrl", car.getImageUrl());
        history.put("acceptedAt", System.currentTimeMillis());
        return history;
    }

    private void sendSmsToBuyer(Proposal proposal, String finalStatus) {
        fetchUserById(proposal.getBuyerId(), user -> {
            if (user == null || TextUtils.isEmpty(user.getPhone())) {
                return;
            }
            try {
                String message = finalStatus.equals("accepted")
                        ? "Accepted: " + proposal.getCarModel() + ", proposed price " + proposal.getProposedPrice()
                        : "Rejected: " + proposal.getCarModel() + ", proposed price " + proposal.getProposedPrice();
                SmsManager.getDefault().sendTextMessage(user.getPhone(), null, message, null, null);
            } catch (Exception ignored) {
            }
        });
    }

    private void enrichCarsWithOwnerNames(List<Car> cars, CarsCallback callback) {
        if (cars.isEmpty()) {
            callback.onLoaded(cars);
            return;
        }

        AtomicInteger remaining = new AtomicInteger(cars.size());
        for (Car car : cars) {
            if (TextUtils.isEmpty(car.getOwnerId())) {
                car.setOwnerName("Unknown owner");
                if (remaining.decrementAndGet() == 0) {
                    callback.onLoaded(cars);
                }
                continue;
            }

            fetchUserById(car.getOwnerId(), user -> {
                if (user != null) {
                    car.setOwnerName(!TextUtils.isEmpty(user.getName()) ? user.getName() : user.getId());
                    car.setOwnerPhone(user.getPhone());
                } else {
                    car.setOwnerName(car.getOwnerId());
                }
                if (remaining.decrementAndGet() == 0) {
                    callback.onLoaded(cars);
                }
            });
        }
    }

    private void enrichProposalsWithBuyerNames(List<Proposal> proposals, ProposalsCallback callback) {
        if (proposals.isEmpty()) {
            callback.onLoaded(proposals);
            return;
        }

        AtomicInteger remaining = new AtomicInteger(proposals.size());
        for (Proposal proposal : proposals) {
            if (!TextUtils.isEmpty(proposal.getBuyerName())) {
                if (remaining.decrementAndGet() == 0) {
                    callback.onLoaded(proposals);
                }
                continue;
            }

            fetchUserById(proposal.getBuyerId(), user -> {
                if (user != null) {
                    proposal.setBuyerName(!TextUtils.isEmpty(user.getName()) ? user.getName() : user.getId());
                } else {
                    proposal.setBuyerName(proposal.getBuyerId());
                }
                if (remaining.decrementAndGet() == 0) {
                    callback.onLoaded(proposals);
                }
            });
        }
    }

    private Car mapCar(DocumentSnapshot doc) {
        Car car = new Car();
        car.setId(doc.getId());
        car.setName(firstNonEmpty(doc.getString("carName"), doc.getString("name")));
        car.setOwnerId(firstNonEmpty(doc.getString("ownerId"), doc.getString("ownerUid")));
        car.setImageUrl(firstNonEmpty(doc.getString("imageUrl"), null));
        car.setImageBase64(firstNonEmpty(doc.getString("imageBase64"), null));
        car.setModel(firstNonEmpty(doc.getString("model"), null));
        car.setOwnerName(firstNonEmpty(doc.getString("ownerName"), null));

        Object priceValue = doc.get("price");
        if (priceValue instanceof Number) {
            car.setPrice(((Number) priceValue).doubleValue());
        } else if (priceValue instanceof String) {
            try {
                car.setPrice(Double.parseDouble((String) priceValue));
            } catch (NumberFormatException ignored) {
                car.setPrice(0d);
            }
        }
        return car;
    }

    private HistoryCar mapHistoryCar(DocumentSnapshot doc) {
        HistoryCar historyCar = doc.toObject(HistoryCar.class);
        if (historyCar == null) {
            historyCar = new HistoryCar();
        }
        historyCar.setId(doc.getId());
        if (TextUtils.isEmpty(historyCar.getCarId())) historyCar.setCarId(doc.getString("carId"));
        if (TextUtils.isEmpty(historyCar.getBuyerId())) historyCar.setBuyerId(doc.getString("buyerId"));
        if (TextUtils.isEmpty(historyCar.getBuyerName())) historyCar.setBuyerName(doc.getString("buyerName"));
        if (TextUtils.isEmpty(historyCar.getOwnerId())) historyCar.setOwnerId(doc.getString("ownerId"));
        if (TextUtils.isEmpty(historyCar.getOwnerName())) historyCar.setOwnerName(doc.getString("ownerName"));
        if (TextUtils.isEmpty(historyCar.getOwnerPhone())) historyCar.setOwnerPhone(doc.getString("ownerPhone"));
        if (TextUtils.isEmpty(historyCar.getCarName())) historyCar.setCarName(firstNonEmpty(doc.getString("carName"), doc.getString("name")));
        if (TextUtils.isEmpty(historyCar.getModel())) historyCar.setModel(doc.getString("model"));
        if (historyCar.getPrice() == 0d) {
            Object priceValue = doc.get("price");
            if (priceValue instanceof Number) {
                historyCar.setPrice(((Number) priceValue).doubleValue());
            }
        }
        if (historyCar.getAcceptedPrice() == 0d) {
            Object acceptedPrice = doc.get("acceptedPrice");
            if (acceptedPrice instanceof Number) {
                historyCar.setAcceptedPrice(((Number) acceptedPrice).doubleValue());
            }
        }
        if (TextUtils.isEmpty(historyCar.getImageBase64())) historyCar.setImageBase64(doc.getString("imageBase64"));
        if (TextUtils.isEmpty(historyCar.getImageUrl())) historyCar.setImageUrl(doc.getString("imageUrl"));
        Object acceptedAt = doc.get("acceptedAt");
        if (acceptedAt instanceof Number) {
            historyCar.setAcceptedAt(((Number) acceptedAt).longValue());
        }
        return historyCar;
    }

    private String firstNonEmpty(String primary, String fallback) {
        if (!TextUtils.isEmpty(primary)) {
            return primary;
        }
        return fallback;
    }

    private Proposal mapProposal(DocumentSnapshot doc) {
        Proposal proposal = doc.toObject(Proposal.class);
        if (proposal == null) {
            proposal = new Proposal();
        }
        proposal.setId(doc.getId());
        if (TextUtils.isEmpty(proposal.getCarId())) {
            proposal.setCarId(doc.getString("carId"));
        }
        if (TextUtils.isEmpty(proposal.getBuyerId())) {
            proposal.setBuyerId(doc.getString("buyerId"));
        }
        if (TextUtils.isEmpty(proposal.getBuyerName())) {
            proposal.setBuyerName(doc.getString("buyerName"));
        }
        if (TextUtils.isEmpty(proposal.getOwnerId())) {
            proposal.setOwnerId(doc.getString("ownerId"));
        }
        if (TextUtils.isEmpty(proposal.getCarModel())) {
            proposal.setCarModel(doc.getString("carModel"));
        }
        if (TextUtils.isEmpty(proposal.getStatus())) {
            proposal.setStatus(doc.getString("status"));
        }
        Number price = doc.getDouble("proposedPrice");
        if (price != null) {
            proposal.setProposedPrice(price.doubleValue());
        }
        Number timestamp = doc.getDouble("timestamp");
        if (timestamp != null) {
            proposal.setTimestamp(timestamp.longValue());
        } else {
            Long ts = doc.getLong("timestamp");
            if (ts != null) {
                proposal.setTimestamp(ts);
            }
        }
        return proposal;
    }

    private User mapUser(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }

        User user = new User();
        user.setId(snapshot.getId());
        String name = snapshot.getString("name");
        if (TextUtils.isEmpty(name)) {
            name = snapshot.getString("username");
        }
        user.setName(name);
        String phone = snapshot.getString("phone");
        user.setPhone(phone);
        return user;
    }
}

