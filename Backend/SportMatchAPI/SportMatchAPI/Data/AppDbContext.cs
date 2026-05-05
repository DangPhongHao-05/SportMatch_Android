using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;
using SportMatchAPI.Models;

namespace SportMatchAPI.Data;

public partial class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Matchinteraction> Matchinteractions { get; set; }

    public virtual DbSet<Matchrequest> Matchrequests { get; set; }

    public virtual DbSet<Message> Messages { get; set; }

    public virtual DbSet<User> Users { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder
            .UseCollation("utf8mb4_unicode_ci")
            .HasCharSet("utf8mb4");

        modelBuilder.Entity<Matchinteraction>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PRIMARY");

            entity.ToTable("matchinteractions");

            entity.HasIndex(e => e.UserId, "UserId");

            entity.HasIndex(e => new { e.MatchRequestId, e.UserId }, "uq_match_user").IsUnique();

            entity.Property(e => e.CreatedAt)
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime");
            entity.Property(e => e.InteractionType)
                .HasMaxLength(20)
                .HasDefaultValueSql("'Apply'");
            entity.Property(e => e.Message).HasColumnType("text");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .HasDefaultValueSql("'Pending'");

            entity.HasOne(d => d.MatchRequest).WithMany(p => p.Matchinteractions)
                .HasForeignKey(d => d.MatchRequestId)
                .HasConstraintName("matchinteractions_ibfk_1");

            entity.HasOne(d => d.User).WithMany(p => p.Matchinteractions)
                .HasForeignKey(d => d.UserId)
                .HasConstraintName("matchinteractions_ibfk_2");
        });

        modelBuilder.Entity<Matchrequest>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PRIMARY");

            entity.ToTable("matchrequests");

            entity.HasIndex(e => e.HostId, "HostId");

            entity.HasIndex(e => e.Location, "spx_location")
                .HasAnnotation("MySql:IndexPrefixLength", new[] { 32 })
                .HasAnnotation("MySql:SpatialIndex", true);

            entity.Property(e => e.CreatedAt)
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime");
            entity.Property(e => e.Description).HasColumnType("text");
            entity.Property(e => e.EndTime).HasColumnType("datetime");
            entity.Property(e => e.MissingPlayers).HasDefaultValueSql("'1'");
            entity.Property(e => e.RequestType)
                .HasMaxLength(20)
                .HasDefaultValueSql("'FindPlayer'");
            entity.Property(e => e.SportType).HasMaxLength(50);
            entity.Property(e => e.StartTime).HasColumnType("datetime");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .HasDefaultValueSql("'Open'");

            entity.HasOne(d => d.Host).WithMany(p => p.Matchrequests)
                .HasForeignKey(d => d.HostId)
                .HasConstraintName("matchrequests_ibfk_1");
        });

        modelBuilder.Entity<Message>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PRIMARY");

            entity.ToTable("messages");

            entity.HasIndex(e => e.ReceiverId, "ReceiverId");

            entity.HasIndex(e => new { e.SenderId, e.ReceiverId, e.SentAt }, "idx_chat_history");

            entity.Property(e => e.Content).HasColumnType("text");
            entity.Property(e => e.IsRead).HasDefaultValueSql("'0'");
            entity.Property(e => e.SentAt)
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime");

            entity.HasOne(d => d.Receiver).WithMany(p => p.MessageReceivers)
                .HasForeignKey(d => d.ReceiverId)
                .HasConstraintName("messages_ibfk_2");

            entity.HasOne(d => d.Sender).WithMany(p => p.MessageSenders)
                .HasForeignKey(d => d.SenderId)
                .HasConstraintName("messages_ibfk_1");
        });

        modelBuilder.Entity<User>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PRIMARY");

            entity.ToTable("users");

            entity.HasIndex(e => e.FirebaseUid, "FirebaseUid").IsUnique();

            entity.HasIndex(e => e.PhoneNumber, "PhoneNumber").IsUnique();

            entity.Property(e => e.AvatarUrl).HasMaxLength(500);
            entity.Property(e => e.Bio).HasColumnType("text");
            entity.Property(e => e.CreatedAt)
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime");
            entity.Property(e => e.FirebaseUid).HasMaxLength(128);
            entity.Property(e => e.FullName).HasMaxLength(100);
            entity.Property(e => e.PhoneNumber).HasMaxLength(20);
            entity.Property(e => e.UpdatedAt)
                .ValueGeneratedOnAddOrUpdate()
                .HasDefaultValueSql("CURRENT_TIMESTAMP")
                .HasColumnType("datetime");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
