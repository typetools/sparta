#!/usr/bin/env perl

use strict;
use warnings;
use Getopt::Long;

# Dump string literals from source and AndroidMainifest.xml
#
# Parameters:
#
# 1) outfile: File to output
#
# 2) dirs: Directories to search for string literals

my @dirs_default = qw/src AndroidManifest.xml/;
my @dirs = ();
my $outfile = "sparta_strings.txt";
GetOptions('dirs:s' => \@dirs, 'outfile:s' => \$outfile);
if ($#dirs < 0) {
    @dirs = @dirs_default;
}

# Grep for double quotes in given directories (or files).
my $dirs_str = join(' ', @dirs);
my @lines = `egrep -R '\"[^"]+\"' $dirs_str`;
my %out = ();
# Break out multiple literals on the same line
for my $line (@lines) {
    while ($line =~ /"([^"]+)"/g) {
        my $match = $1;
        # Strip ends of whitespace
        $match =~ s/^\s+//;
        $match =~ s/\s+$//;

        # Make unique and don't include lines with whitespace only
        $out{$match} = $match if $match =~ '[^ ]+';
    }
}

@lines = `egrep -R '[^/]>[^<]+<' $dirs_str`;
# Break out multiple literals on the same line
for my $line (@lines) {
    while ($line =~ /[^\/]>([^<]+)</g) {
        my $match = $1;
        # Strip ends of whitespace
        $match =~ s/^\s+//;
        $match =~ s/\s+$//;

        # Make unique and don't include lines with whitespace only
        $out{$match} = $match if $match =~ '[^ ]+';
    }
}

my @lines_uniq = keys %out;
@lines_uniq = sort (@lines_uniq);
# print join("\n", @lines_uniq);

open (my $FH , ">", $outfile) or die "cannot open $outfile: $!";
for my $line (@lines_uniq) {
    print $FH "$line\n";
}
close($FH)


