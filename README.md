# Apni_svari Buyer/Seller Proposal Flow

## What was added
- Buyer home feed in `HomeFragment`
- Car detail screen in `CarDetailFragment`
- Seller proposal inbox in `ProposalsFragment`
- RecyclerView adapters for cars and proposals
- Firestore integration for `cars`, `users`, `proposals`, and `messages`
- SMS permission support for accept/reject notifications

## Firestore collections
### `cars`
- `ownerId`
- `ownerUid`
- `ownerName`
- `ownerPhone`
- `name` / `carName`
- `model`
- `price`
- `imageBase64` or `imageUrl`
- `createdAt`

### `users`
- `username` or `name`
- `phone`
- `uid`

### `proposals`
- `carId`
- `buyerId`
- `buyerName`
- `ownerId`
- `proposedPrice`
- `carModel`
- `status` (`pending`, `accepted`, `rejected`)
- `timestamp`

### `messages`
- `carId`
- `buyerId`
- `ownerId`
- `carModel`
- `proposedPrice`
- `status`
- `text`
- `timestamp`

## Navigation
- Buyer side: `MainUserPage` -> `HomeFragment` -> `CarDetailFragment`
- Seller side: `ZMainSellerPage` -> `ProposalsFragment`

## Notes
- SMS permission is declared in `AndroidManifest.xml`.
- `ZsellerHome` now writes seller cars only to `cars`, which is the single product collection used by both buyer and seller screens.

