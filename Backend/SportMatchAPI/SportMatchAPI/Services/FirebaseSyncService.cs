using FirebaseAdmin;
using Google.Cloud.Firestore;
using Google.Apis.Auth.OAuth2;
using Google.Cloud.Firestore.V1;

public class FirebaseSyncService
{
    private readonly FirestoreDb _db;

    public FirebaseSyncService(string projectId, string pathToKey)
    {
        var credential = GoogleCredential.FromFile(pathToKey);

        var builder = new FirestoreClientBuilder
        {
            Credential = credential
        };

        _db = new FirestoreDbBuilder
        {
            ProjectId = projectId,
            Credential = credential
        }.Build();
    }

    public async Task SyncUser(int userId, string fullName, string avatarUrl)
    {
        var docRef = _db.Collection("Users").Document(userId.ToString());
        var data = new Dictionary<string, object>
        {
            { "fullName", fullName },
            { "avatarUrl", avatarUrl ?? "default" }
        };
        await docRef.SetAsync(data, SetOptions.MergeAll);
    }
}