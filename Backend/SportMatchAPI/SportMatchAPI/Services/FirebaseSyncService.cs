using FirebaseAdmin;
using Google.Cloud.Firestore;
using Google.Apis.Auth.OAuth2;
using Google.Cloud.Firestore.V1;

namespace SportMatchAPI.Services;

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
        try
        {
            Console.WriteLine($"==> [FIREBASE] Bắt đầu đẩy dữ liệu User {userId} lên Firestore...");

            var docRef = _db.Collection("Users").Document(userId.ToString());
            var data = new Dictionary<string, object>
            {
                { "fullName", fullName },
                { "avatarUrl", avatarUrl ?? "default" }
            };

            await docRef.SetAsync(data, SetOptions.MergeAll);

            Console.WriteLine($"==> [FIREBASE] THÀNH CÔNG: Đã đồng bộ User {userId}!");
        }
        catch (Exception ex)
        {
            // Bắt bằng được lỗi nếu Firebase từ chối kết nối
            Console.WriteLine($"==> [FIREBASE LỖI NGHIÊM TRỌNG]: {ex.Message}");
            Console.WriteLine(ex.StackTrace);
        }
    }
}