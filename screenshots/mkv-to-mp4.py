#Convert MKV files to MP4 format using ffmpeg
import os
import subprocess
import sys

def convert_mkv_to_mp4(input_file, output_file):
    command = ['ffmpeg', '-i', input_file, '-codec', 'copy', output_file]
    try:
        subprocess.run(command, check=True)
        print(f"Converted: {input_file} to {output_file}")
    except subprocess.CalledProcessError as e:
        print(f"Error converting {input_file}: {e}")

def main():
    if len(sys.argv) != 3:
        print("Usage: python mkv-to-mp4.py <input_directory> <output_directory>")
        sys.exit(1)

    input_directory = sys.argv[1]
    output_directory = sys.argv[2]

    if not os.path.exists(output_directory):
        os.makedirs(output_directory)

    for filename in os.listdir(input_directory):
        if filename.endswith('.mkv'):
            input_file = os.path.join(input_directory, filename)
            output_file = os.path.join(output_directory, filename[:-4] + '.mp4')
            convert_mkv_to_mp4(input_file, output_file)

if __name__ == "__main__":
    main()