#!/usr/bin/env perl

use strict;
use warnings;
use File::stat;
use Getopt::Long;

# Finds all source-strings recursively from root-dir
# and compares to strings in string-file
#
# Parameters:
# 1) source-strings: file to read strings from
#
# 2) search-file: file to search for
#
# 3) root-dir: root directory to recursively search for search-file

my $search_file ="sparta_strings.txt";
my $source_strings ="sparta-output/sparta_strings.txt";
my $root_dir ="../";
GetOptions('root-dir:s' => \$root_dir,
        'source-strings:s' => \$source_strings, 'search_file:s' => \$search_file);

my $local_stat = stat($source_strings) or die "Failed to stat input file $source_strings: $!";
my @string_files = `find $root_dir -type f -name $search_file`;

for my $string_file (@string_files) {
    print "\n";
    chomp($string_file);
    my $current_stat = stat($string_file);
    if ($current_stat->ino == $local_stat->ino
        && $current_stat->dev == $local_stat->dev) {

        print "Not checking $search_file against itself.\n";
        next;
    }

    my @matches = `cat $source_strings $string_file | sort | uniq -c | sort -n | grep -v '   1'`;
    print "$string_file matches to $source_strings:\n";
    print @matches;
}
