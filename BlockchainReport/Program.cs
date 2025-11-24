using System.Security.Cryptography;
using System.Text;

// --- 1. Execution ---
Console.WriteLine("\n--- FINAL PROJECT: Blockchain Investigation (SHA-256) ---");

// A. Create the Genesis Block
var block1 = new Block("Genesis: Alice pays Bob 10 BTC", "0");
Console.WriteLine($"[Block 1] Hash: {block1.Hash}");

// B. Create Block 2 (Linked to Block 1)
var block2 = new Block("Block 2: Bob pays Charlie 5 BTC", block1.Hash);
Console.WriteLine($"[Block 2] PreviousHash: {block2.PreviousHash}");
Console.WriteLine($"[Block 2] Hash:         {block2.Hash}");

// C. Tamper Evidence Test
Console.WriteLine("\n--- Tamper-Evidence Test ---");
Console.WriteLine("Attempting to change data in Block 1...");

// If we change the data, the hash MUST change
var tamperedBlock1 = new Block("Genesis: Alice pays Bob 1000000 BTC", "0");

Console.WriteLine($"[Original Block 1 Hash]: {block1.Hash}");
Console.WriteLine($"[Tampered Block 1 Hash]: {tamperedBlock1.Hash}");
Console.WriteLine($"[Block 2 PreviousHash]:  {block2.PreviousHash}");

if (block2.PreviousHash != tamperedBlock1.Hash)
{
    Console.WriteLine("\nRESULT: The chain is BROKEN. Block 2 points to a hash that no longer exists.");
}

// --- 2. Class Definition ---
public class Block
{
    public string PreviousHash { get; set; }
    public string Data { get; set; }
    public long TimeStamp { get; set; }
    public string Hash { get; private set; }

    public Block(string data, string previousHash)
    {
        Data = data;
        PreviousHash = previousHash;
        TimeStamp = DateTimeOffset.Now.ToUnixTimeMilliseconds();
        Hash = CalculateHash();
    }

    public string CalculateHash()
    {
        using (var sha256 = SHA256.Create())
        {
            // Combine previous hash + timestamp + data
            string rawData = PreviousHash + TimeStamp + Data;

            byte[] bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(rawData));

            // Convert to hex string
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < bytes.Length; i++)
            {
                builder.Append(bytes[i].ToString("x2"));
            }
            return builder.ToString();
        }
    }
}