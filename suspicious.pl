#!/usr/bin/env perl

use strict;
use warnings;
use Regexp::Common qw/URI/;
use Regexp::Common qw/net/;


# Script to analyze a bunch of source files for suspicious content.
#
# Parameters:
#
# 1) root-dir: root directory to recursively search for files
#
# 2) pattern: custom regexp pattern to search for 
#             (replaces the built-in ones) - optional parameter
#
#
$#ARGV>=0 || die "usage: $0 'root-dir' [pattern]";

# name pattern of files to include in analysis
my $file_pattern="(\.java|[sS]trings\.xml)\$";

# default search pattern:
#  - content prefix (e.g., used in content://...)
#  - http prefix (e.g., used in String concatenation)
#  - URIs
#  - IPv4 addresses
#  - MAC addresses
#  - dangerous file mode
my $search_pattern =
"(\"\\s*content[^\"]*\"|\"\\s*http[^\"]*\"|$RE{URI}{-keep}|$RE{net}{IPv4}{-keep}|$RE{net}{MAC}{-keep}|MODE_WORLD_WRITEABLE)";

# use specific pattern if provided via cmd
$search_pattern=$ARGV[1] if $#ARGV==1;

# recursively analyze directory
process_dir ($ARGV[0]);

sub process_dir{
    my $root_dir = shift;

    opendir (DIR, $root_dir) or die "Unable to open directory \'$root_dir\': $!";

    my @files = grep !/^\.{1,2}$/ , readdir (DIR);
    closedir (DIR);
    @files = map { $root_dir . '/' . $_ } @files;

    for (@files) {
        # analyze sub-directories if this file is a directory
        if (-d $_) {
            process_dir($_);
        } else {
            # check for defined file name pattern and analyze files
            analyze_file($_) if /$file_pattern/; 
        }
    }
}

sub analyze_file{
    my $file=$_;
    my $line=1;
    open(IN, "<$file") or die "Cannot open file \'$file\': $!";

    while(<IN>){
        print "$file($line): Contains $1\n" if /$search_pattern/;

        ++$line
    }
}
