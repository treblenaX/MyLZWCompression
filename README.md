# MyLZWCompression
My version of the LZW compression. When encoding, it outputs the values with 9-bits then increments the bit length as it exceeds the limit. (9-bits for values 256-511, 10-bits for values 512-2047, etc.)

Needs my I/O Binary Streams (v1.0.0) : https://github.com/treblenaX/BinaryStream
