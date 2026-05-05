using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;

namespace SportMatchAPI.Models;

public partial class Matchrequest
{
    public int Id { get; set; }

    public int HostId { get; set; }

    public string? RequestType { get; set; }

    public string SportType { get; set; } = null!;

    public int? MissingPlayers { get; set; }

    public string? Description { get; set; }

    public DateTime StartTime { get; set; }

    public DateTime EndTime { get; set; }

    public Point Location { get; set; } = null!;

    public string? Status { get; set; }

    public DateTime? CreatedAt { get; set; }

    public virtual User Host { get; set; } = null!;

    public virtual ICollection<Matchinteraction> Matchinteractions { get; set; } = new List<Matchinteraction>();
}
