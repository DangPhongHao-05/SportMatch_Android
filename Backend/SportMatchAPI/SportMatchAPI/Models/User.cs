using System;
using System.Collections.Generic;

namespace SportMatchAPI.Models;

public partial class User
{
    public int Id { get; set; }

    public string? FirebaseUid { get; set; }

    public string PhoneNumber { get; set; } = null!;

    public string FullName { get; set; } = null!;

    public string? AvatarUrl { get; set; }

    public string? Bio { get; set; }

    public DateTime? CreatedAt { get; set; }

    public DateTime? UpdatedAt { get; set; }

    public string? FcmToken { get; set; }

    public virtual ICollection<Matchinteraction> Matchinteractions { get; set; } = new List<Matchinteraction>();

    public virtual ICollection<Matchrequest> Matchrequests { get; set; } = new List<Matchrequest>();

    public virtual ICollection<Message> MessageReceivers { get; set; } = new List<Message>();

    public virtual ICollection<Message> MessageSenders { get; set; } = new List<Message>();
}
