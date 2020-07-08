package main

import (
	"bufio"
	"bytes"
	"encoding/json"
	"fmt"
	"os"
	"regexp"
	"strconv"
	"strings"
	"time"
)

func main() {
	var scanner *bufio.Scanner
	if len(os.Args) > 1 {
		file, err := os.Open(os.Args[1])
		if err != nil {
			panic(err)
		}
		defer file.Close()
		scanner = bufio.NewScanner(file)
	} else {
		scanner = bufio.NewScanner(os.Stdin)
	}

	lines := read(scanner)
	var entries []Entry
	p, _ := parse(lines)
	for _, ch := range p {
		e, err := parseEntry(ch)
		if err == nil {
			entries = append(entries, e)
		}
	}
	for _, e := range entries {
		j, err := e.JSON()
		if err == nil {
			fmt.Println(string(j))
		}

	}
}

func (t *Entry) JSON() ([]byte, error) {
	buffer := &bytes.Buffer{}
	encoder := json.NewEncoder(buffer)
	encoder.SetEscapeHTML(false)
	err := encoder.Encode(t)
	return buffer.Bytes(), err
}

type Entry struct {
	ReqMethod  string            `json:"requestMethod"`
	ReqUrl     string            `json:"requestUrl"`
	ReqTime    time.Time         `json:"requestTime"`
	ReqHeaders map[string]string `json:"requestHeaders"`
	ReqBody    string            `json:"requestBody"`
	RspCode    int               `json:"responseCode"`
	RspTime    time.Time         `json:"responseTime"`
	RspHeaders map[string]string `json:"responseHeaders"`
	RspBody    string            `json:"responseBody"`
}

func parse(lines []string) ([][]string, error) {
	var out [][]string
	var tmp []string
	for _, l := range lines {
		if strings.HasSuffix(l, "Client out-bound request") && len(tmp) > 0 {
			out = append(out, tmp)
			tmp = []string{}
		}
		tmp = append(tmp, l)
	}
	if len(tmp) > 0 {
		out = append(out, tmp)
	}
	return out, nil
}

func parseEntry(lines []string) (Entry, error) {
	out := Entry{
		ReqMethod:  "",
		ReqUrl:     "",
		ReqTime:    time.Time{},
		ReqHeaders: map[string]string{},
		ReqBody:    "",
		RspCode:    0,
		RspTime:    time.Time{},
		RspHeaders: map[string]string{},
		RspBody:    "",
	}
	isRsp := false
	for _, l := range lines {
		attr, s := parseLine(l)
		if attr == Req {
			isRsp = false
			continue
		}
		if attr == Rsp {
			isRsp = true
			continue
		}
		if attr == MethodUrl {
			f := strings.Fields(s)
			out.ReqMethod = f[0]
			out.ReqUrl = f[1]
			continue
		}
		if attr == Date {
			s = strings.Replace(s, "--", "T", 1)
			s = s + "+02:00"
			t, err := time.Parse(time.RFC3339, s)
			if err != nil {
				continue
			}
			if isRsp {
				out.RspTime = t
			} else {
				out.ReqTime = t
			}
			continue
		}
		if attr == Header {
			f := strings.Split(s, ":")
			if isRsp {
				out.RspHeaders[f[0]] = f[1]
			} else {
				out.ReqHeaders[f[0]] = f[1]
			}
			continue
		}
		if attr == Body {
			if isRsp {
				out.RspBody = s
			} else {
				out.ReqBody = s
			}
			continue
		}
		if attr == Status {
			code, err := strconv.Atoi(s)
			if err == nil {
				out.RspCode = code
			}
			continue
		}
	}
	return out, nil
}

func parseLine(s string) (Attr, string) {
	if info.MatchString(s) {
		if strings.HasSuffix(s, "Client out-bound request") {
			return Req, ""
		}
		if strings.HasSuffix(s, "Client in-bound response") {
			return Rsp, ""
		}
		if date.MatchString(s) {
			return Date, date.FindString(s)
		}
	}
	if reqOrRes.MatchString(s) {
		if status.MatchString(s) {
			return Status, code.FindString(s)
		}
		if methodUrl.MatchString(s) {
			return MethodUrl, methodUrl.FindString(s)
		}
		if header.MatchString(s) {
			return Header, header.FindString(s)
		}
	}
	if body.MatchString(s) {
		return Body, body.FindString(s)
	}
	return Default, ""
}

var (
	info      = regexp.MustCompile(`^\d+ [*] .*`)
	reqOrRes  = regexp.MustCompile(`^\d+ [<>] .*`)
	num       = regexp.MustCompile(`^\d+`)
	date      = regexp.MustCompile(`\d\d\d\d-\d\d-\d\d--\d\d:\d\d:\d\d.\d\d\d`)
	methodUrl = regexp.MustCompile(`[A-Z]* https://.*$`)
	header    = regexp.MustCompile(`\S*: .*$`)
	body      = regexp.MustCompile(`^[^\d].*$`)
	status    = regexp.MustCompile(`^\d+ < \d+$`)
	code      = regexp.MustCompile(`\d+$`)
)

type Attr int

const (
	Req Attr = iota
	Rsp
	Date
	MethodUrl
	Header
	Body
	Status
	Default
)

func read(scanner *bufio.Scanner) []string {
	scanner.Split(bufio.ScanLines)
	var lines []string
	const maxCapacity = 1024 * 1024
	buf := make([]byte, 0, maxCapacity)
	scanner.Buffer(buf, maxCapacity)
	for scanner.Scan() {
		lines = append(lines, scanner.Text())
	}
	return lines
}
